/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex.test;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.jboss.jandex.TypeVariable;
import org.junit.Assert;
import org.junit.Test;

public class RecursiveBoundsTest {

    static final DotName SCORE_MANAGER_FACTORY_NAME = DotName.createSimple(ScoreManagerFactory.class.getName());
    static final DotName SCORE_NAME = DotName.createSimple(Score.class.getName());
    static final DotName SCORE_MANAGER_NAME = DotName.createSimple(ScoreManager.class.getName());

    @Test
    public void testIndexView() throws IOException {
        Index index = getIndexForClass(SCORE_NAME, SCORE_MANAGER_NAME, SCORE_MANAGER_FACTORY_NAME);
        ClassInfo scoreManager = index.getClassByName(SCORE_MANAGER_FACTORY_NAME);
        MethodInfo newScoreManager = scoreManager.method("newScoreManager");
        Type returnType = newScoreManager.returnType();
        // ScoreManager<S>
        Assert.assertEquals(Kind.PARAMETERIZED_TYPE, returnType.kind());
        // S -> S extends Score<S>
        TypeVariable returnTypeVariable = returnType.asParameterizedType().arguments().get(0).asTypeVariable();
        // Score<S>
        ParameterizedType sBound = returnTypeVariable.bounds().get(0).asParameterizedType();
        // S -> recursive bound... -> S extends Score<S>
        Type sBoundArgument = sBound.arguments().get(0);
        Type sBoundArgumentTypeVariable = sBoundArgument.asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0);
        Assert.assertEquals(Kind.TYPE_VARIABLE, sBoundArgumentTypeVariable.kind());
    }

    interface Score<S extends Score<S>> {
    }

    public static class ScoreManager<S extends Score<S>> {

    }

    public static class ScoreManagerFactory {

        public <S extends Score<S>> ScoreManager<S> newScoreManager() {
            return new ScoreManager<>();
        }

    }

    private Index getIndexForClass(DotName... classes) throws IOException {
        Indexer indexer = new Indexer();
        for (DotName clazz : classes) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(clazz.toString().replace('.', '/') + ".class");
            indexer.index(stream);
        }
        return indexer.complete();
    }

}
