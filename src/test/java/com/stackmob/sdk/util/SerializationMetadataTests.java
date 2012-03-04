/**
 * Copyright 2012 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stackmob.sdk.util;

import com.stackmob.sdk.StackMobTestCommon;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.testobjects.Author;
import com.stackmob.sdk.util.SerializationMetadata;
import static com.stackmob.sdk.util.SerializationMetadata.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;

public class SerializationMetadataTests extends StackMobTestCommon {

    private class CrazyStuff extends StackMobModel {
        public CrazyStuff() {
            super(CrazyStuff.class);
        }
        private String foo = "foo";
        private int bar = 6;
        private long x = 1337;
        private UUID uuid = new UUID(3,4);
        private String[] strings = new String[] {"hello", "world"};
        private boolean test = false;
        private byte[] myBytes = new byte[] {(byte)0xaf, (byte)0x45, (byte)0xf3};
        private CountDownLatch Latch = latchOne();
        private Author author;
        private Author[] authors;
        private List<Author> authorList;
    }
    
    @Test public void testSerializationMetaData() throws Exception {
        ensureMetadata(CrazyStuff.class);
        assertEquals(PRIMITIVE,getSerializationMetadata(CrazyStuff.class,"foo"));
        assertEquals(PRIMITIVE,getSerializationMetadata(CrazyStuff.class,"bar"));
        assertEquals(PRIMITIVE,getSerializationMetadata(CrazyStuff.class,"x"));
        assertEquals(OBJECT,getSerializationMetadata(CrazyStuff.class,"uuid"));
        assertEquals(OBJECT_ARRAY,getSerializationMetadata(CrazyStuff.class,"strings"));
        assertEquals(PRIMITIVE,getSerializationMetadata(CrazyStuff.class,"test"));
        assertEquals(OBJECT_ARRAY,getSerializationMetadata(CrazyStuff.class,"myBytes"));
        assertEquals(OBJECT,getSerializationMetadata(CrazyStuff.class,"Latch"));
        assertEquals(MODEL,getSerializationMetadata(CrazyStuff.class,"author"));
        assertEquals(MODEL_ARRAY,getSerializationMetadata(CrazyStuff.class,"authors"));
        assertEquals(OBJECT,getSerializationMetadata(CrazyStuff.class,"authorList"));
    }
}
