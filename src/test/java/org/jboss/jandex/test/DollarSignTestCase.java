/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019 Red Hat, Inc., and individual contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.Test;

public class DollarSignTestCase {

    @Test
    public void testClass() throws IOException {
        Index index = createIndex(Dollar$Sign.class);
        ClassInfo info = index.getKnownClasses().iterator().next();
        assertFalse(info.name().isInner());
        assertTrue(info.name().isComponentized());
        assertEquals("org.jboss.jandex.test.Dollar$Sign", info.name().toString());
        assertEquals("Dollar$Sign", info.name().local());
    }
    
    @Test
    public void testNestedClass() throws IOException {
        Index index = createIndex(Foo$$Nice.class);
        ClassInfo info = index.getKnownClasses().iterator().next();
        assertTrue(info.name().isInner());
        assertTrue(info.name().isComponentized());
        assertEquals("org.jboss.jandex.test.DollarSignTestCase$Foo$$Nice", info.name().toString());
        assertEquals("Foo$$Nice", info.name().local());
    }

    private Index createIndex(Class<?> clazz) throws IOException {
        Indexer indexer = new Indexer();
        try (InputStream is = DollarSignTestCase.class.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class")) {
            indexer.index(is);
        }
        return indexer.complete();
    }

    static class Foo$$Nice {

    }

}
