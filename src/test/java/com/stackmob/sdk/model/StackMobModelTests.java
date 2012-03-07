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

package com.stackmob.sdk.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stackmob.sdk.StackMobTestCommon;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.model.StackMobModel;
import com.stackmob.sdk.testobjects.Author;
import com.stackmob.sdk.testobjects.Book;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;
import static org.junit.Assert.*;

public class StackMobModelTests extends StackMobTestCommon {
    
    /* Offline */

    private class Simple extends StackMobModel {
        public Simple() {
            super(Simple.class);
        }
        public Simple(Class<? extends StackMobModel> actualClass) {
            super(actualClass);
        }
        protected String foo = "test";
        protected int bar = 5;
    }
    
    @Test public void testBasicBehavior() throws Exception {
        Simple simple = new Simple();
        assertEquals("simple", simple.getSchemaName());
        assertEquals("simple_id", simple.getIDFieldName());
        assertEquals("{\"foo\":\"test\",\"bar\":5}", simple.toJSON());
    }
    
    private class Complicated extends Simple {
        public Complicated() {
            super(Complicated.class);
        }
        private long number = 1337;
        private UUID uuid = new UUID(3,4);
        private String[] strings = new String[] {"hello", "world"};
        private boolean test = false;
        private byte[] myBytes = new byte[] {(byte)0xaf, (byte)0x45, (byte)0xf3};
        private CountDownLatch Latch = latchOne();
    }
    
    @Test public void testComplicatedTypes() throws Exception {
        String json = new Complicated().toJSON();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        assertTrue(object.get("foo").getAsJsonPrimitive().isString());
        assertTrue(object.get("bar").getAsJsonPrimitive().isNumber());
        assertTrue(object.get("number").getAsJsonPrimitive().isNumber());
        assertTrue(object.get("uuid").getAsJsonPrimitive().isString());
        assertTrue(object.get("strings").isJsonArray() && object.get("strings").getAsJsonArray().iterator().next().getAsJsonPrimitive().isString());
        assertTrue(object.get("test").getAsJsonPrimitive().isBoolean());
        assertTrue(object.get("myBytes").isJsonArray() && object.get("myBytes").getAsJsonArray().iterator().next().getAsJsonPrimitive().isNumber());
        assertTrue(object.get("Latch").getAsJsonPrimitive().isString());
    }

    String bookName1 = "The C Programming Language";
    String bookPublisher1 = "Prentice Hall";
    
    @Test public void testFillUnexpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\"," +
                       "\"author\":KnR}";
        System.out.println(json);
        Book book = new Book();
        book.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, book.getTitle());
        assertEquals(bookPublisher1, book.getPublisher());
        assertNotNull(book.getAuthor());
        assertEquals("KnR", book.getAuthor().getID());
        assertNull(book.getAuthor().getName());

    }

    @Test public void testFillExpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\", " +
                       "\"author\":{\"author_id\":\"KnR\", " +
                                     "\"name\":\"Kernighan and Ritchie\"}}";
        Book book = new Book();
        book.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, book.getTitle());
        assertEquals(bookPublisher1, book.getPublisher());
        assertNotNull(book.getAuthor());
        assertEquals("KnR", book.getAuthor().getID());
        assertEquals("Kernighan and Ritchie", book.getAuthor().getName());
    }
    
    @Test public void testFillComplicatedJSON() throws Exception {
        String json = "{\"number\":1338,\"strings\":[\"hello!\",\"world!\"],\"test\":true,\"myBytes\":[1,2,3],\"foo\":\"testpassed\",\"bar\":27,\"uuid\":\"\\\"00000000-0000-0003-0000-000000000005\\\"\",\"Latch\":\"{\\\"sync\\\":{\\\"state\\\":0}}\"}";
        Complicated c = new Complicated();
        c.fillFromJSON(new JsonParser().parse(json));
        assertEquals(c.foo,"testpassed");
        assertEquals(c.bar, 27);
        assertEquals(c.number, 1338);
        assertNotNull(c.uuid);
        assertEquals(c.uuid.toString(), "00000000-0000-0003-0000-000000000005");
        assertNotNull(c.strings);
        assertEquals(c.strings[0], "hello!");
        assertEquals(c.test,true);
        assertNotNull(c.myBytes);
        assertEquals(c.myBytes[0], 1);
        assertNotNull(c.Latch);
        assertEquals(c.Latch.getCount(), 0);
    }
    
    private class Bad_Schema_Name extends StackMobModel {
        public Bad_Schema_Name() {
            super(Bad_Schema_Name.class);
        }    
        String foo = "fail";
    }

    @Test public void testBadSchemaName() throws Exception {
        try {
            new Bad_Schema_Name().toJSON();
            assertTrue(false);
        } catch(Exception e) { }
    }

    private class BadFieldName extends StackMobModel {
        public BadFieldName() {
            super(BadFieldName.class);
        }
        String _foo = "fail";
    }

    @Test public void testBadFieldName() throws Exception {
        try {
            new BadFieldName().toJSON();
            assertTrue(false);
        } catch(Exception e) { }
    }
    
    @Test public void testNestedModels() throws Exception {
        Author a = new Author("Terry Pratchett");
        a.setID("pratchett");
        Book b = new Book("Mort", "Harper Collins", a);
        String json = b.toJSON();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        assertTrue(object.get("title").getAsJsonPrimitive().getAsString().equals("Mort"));
        assertTrue(object.get("publisher").getAsJsonPrimitive().getAsString().equals("Harper Collins"));
        assertTrue(object.get("author").getAsJsonPrimitive().getAsString().equals("pratchett"));
    }
    
    @Test public void testList() throws  Exception {
        List<String> list = new LinkedList<String>();
        list.add("foo");
        list.add("bar");
        list.add("baz");
        String json = gson.toJson(list);
    }


    /* Online */

    String bookName2 = "yet another book!";
    String bookPublisher2 = "no one";
    final MultiThreadAsserter asserter = new MultiThreadAsserter();
    final CountDownLatch latch = latchOne();

    private abstract class AssertErrorCallback extends StackMobCallback {

        @Override
        public void failure(StackMobException e) {
            asserter.markException(e);
        }
    }
    
    @Test public void testSaveToServer()  throws Exception {
        final Book book = new Book();
        book.setTitle(bookName2);
        book.setPublisher(bookPublisher2);
        
        assertNull(book.getID());

        book.init(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(book.getID());
                latch.countDown();
            }
        });

        asserter.assertLatchFinished(latch);
    }

    @Test public void saveComplicatedTypesToServer()  throws Exception {
        final Complicated ls = new Complicated();

        ls.init(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(ls.getID());
                latch.countDown();
            }
        });

        asserter.assertLatchFinished(latch);
    }



    @Test public void testUpdateFromServer() throws Exception {
        final Book book = new Book();
        book.setID("4f511b979ffcad4fd0034c30");
        book.load(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                System.out.println("success");
                asserter.markEquals(book.getTitle(), bookName2);
                asserter.markEquals(book.getPublisher(), bookPublisher2);
                latch.countDown();
            }
        });

        asserter.assertLatchFinished(latch);
    }


    @Test public void testFullSequence() throws Exception {
        final Author author = new Author();
        author.setName("Larry Wall");
        author.init(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                createBook(author);
            }
        });
        
        asserter.assertLatchFinished(latch);
    }
    
    
    public void createBook(Author author) {
        final Book book = new Book();
        book.setID("camelbook");
        book.setTitle("Programming Perl");
        book.setPublisher("O'Reilly");
        book.setAuthor(author);
        try {
            book.init(new AssertErrorCallback() {
                @Override
                public void success(String responseBody) {
                    fetchBook();
                }
            });
        } catch (Exception e) {
            assertTrue(false);
        }
    }
    
    public void fetchBook() {
        final Book book = new Book();
        book.setID("camelbook");
        book.load(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                fetchBookWithExpand();
            }
        });
    }
    
    public void fetchBookWithExpand() {
        final Book book = new Book();
        book.setID("camelbook"); 
        book.setDepth(2);
        book.load(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                updateBook(book);
            }
        });
    }
    
    public void updateBook(Book book) {
        final Book theBook = book;
        book.setTitle("Programming Perl 2: Perl Harder");
        try {
            book.save(new AssertErrorCallback() {
                @Override
                public void success(String responseBody) {
                    deleteBook(theBook);
                }
            });
        } catch (Exception e) {
            assertTrue(false);
        }
    }
    
    public void deleteBook(Book book) {
        book.delete(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                latch.countDown();
            }
        });
    }
}
