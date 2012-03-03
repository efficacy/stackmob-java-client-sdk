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

package com.stackmob.sdk;

import com.google.gson.JsonParser;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.testobjects.Author;
import com.stackmob.sdk.testobjects.Book;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;
import static org.junit.Assert.*;

public class StackMobModelTests extends StackMobTestCommon {
    
    /* Offline */

    private class Simple extends StackMobModel {
        public Simple(StackMob stackmob) {
            this(stackmob, Simple.class);
        }
        public Simple(StackMob stackmob, Class<? extends StackMobModel> actualClass) {
            super(stackmob, actualClass);
        }
        private String foo = "test";
        private int bar = 5;
    }
    
    @Test public void testBasicBehavior() throws Exception {
        Simple simple = new Simple(stackmob);
        assertEquals("simple", simple.getSchemaName());
        assertEquals("simple_id", simple.getIDFieldName());
        assertEquals("{\"foo\":\"test\",\"bar\":5}", simple.toJSON());
    }
    
    private class LessSimple extends Simple {
        public LessSimple(StackMob stackmob) {
            super(stackmob, LessSimple.class);
        }
        private long x = 1337;
        private UUID uuid = new UUID(3,4);
        private String[] strings = new String[] {"hello", "world"};
        private boolean test = false;
        private byte[] myBytes = new byte[] {(byte)0xaf, (byte)0x45, (byte)0xf3};
        private CountDownLatch Latch = latchOne();
    }
    
    @Test public void testComplicatedTypes() throws Exception {
        String json = new LessSimple(stackmob).toJSON();
        assertEquals(json, "{\"x\":1337,\"uuid\":\"00000000-0000-0003-0000-000000000004\",\"strings\":[\"hello\",\"world\"],\"test\":false,\"myBytes\":[-81,69,-13],\"Latch\":{\"sync\":{\"state\":1}},\"foo\":\"test\",\"bar\":5}");
    }
    String bookName1 = "The C Programming Language";
    String bookPublisher1 = "Prentice Hall";

    
    @Test public void testFillUnexpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\"," +
                       "\"author\":KnR}";
        System.out.println(json);
        Book book = new Book(stackmob);
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
        Book book = new Book(stackmob);
        book.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, book.getTitle());
        assertEquals(bookPublisher1, book.getPublisher());
        assertNotNull(book.getAuthor());
        assertEquals("KnR", book.getAuthor().getID());
        assertEquals("Kernighan and Ritchie", book.getAuthor().getName());
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
        final Book book = new Book(stackmob);
        book.setTitle(bookName2);
        book.setPublisher(bookPublisher2);
        
        assertNull(book.getID());

        book.createOnServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(book.getID());
                latch.countDown();
            }
        });

        asserter.assertLatchFinished(latch);
    }

    @Test public void saveComplicatedTypesToServer()  throws Exception {
        final LessSimple ls = new LessSimple(stackmob);

        ls.createOnServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(ls.getID());
                latch.countDown();
            }
        });

        asserter.assertLatchFinished(latch);
    }



    @Test public void testUpdateFromServer() throws Exception {
        final Book book = new Book(stackmob);
        book.setID("4f511b979ffcad4fd0034c30");
        book.loadFromServer(new AssertErrorCallback() {
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
        final Author author = new Author(stackmob);
        author.setName("Larry Wall");
        author.createOnServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                createBook(author);
            }
        });
        
        asserter.assertLatchFinished(latch);
    }
    
    
    public void createBook(Author author) {
        final Book book = new Book(stackmob);
        book.setID("camelbook");
        book.setTitle("Programming Perl");
        book.setPublisher("O'Reilly");
        book.setAuthor(author);
        book.createOnServer( new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                fetchBook();
            }
        });
    }
    
    public void fetchBook() {
        final Book book = new Book(stackmob);
        book.setID("camelbook");
        book.loadFromServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                fetchBookWithExpand();
            }
        });
    }
    
    public void fetchBookWithExpand() {
        final Book book = new Book(stackmob);
        book.setID("camelbook"); 
        book.loadFromServer(2, new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                updateBook(book);
            }
        });
    }
    
    public void updateBook(Book book) {
        final Book theBook = book;
        book.setTitle("Programming Perl 2: Perl Harder");
        book.saveOnServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                deleteBook(theBook);
            }
        });
    }
    
    public void deleteBook(Book book) {
        book.deleteFromServer(new AssertErrorCallback() {
            @Override
            public void success(String responseBody) {
                latch.countDown();
            }
        });
    }
}
