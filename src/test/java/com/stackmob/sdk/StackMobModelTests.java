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

import java.util.concurrent.CountDownLatch;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;
import static org.junit.Assert.*;

public class StackMobModelTests extends StackMobTestCommon {
    
    /* Offline */

    private class Simple extends StackMobModel {
        public Simple(StackMob stackmob) {
            super(stackmob, Simple.class);
        }
        private String foo;
        private int bar;
    }
    
    @Test public void testBasicBehavior() throws Exception {
        Simple simple = new Simple(stackmob);
        simple.foo = "test";
        simple.bar = 5;
        assertEquals("simple", simple.getSchemaName());
        assertEquals("simple_id", simple.getIDFieldName());
        assertEquals("{\"foo\":\"test\",\"bar\":5}", simple.toJSON());
    }

    String bookName1 = "The C Programming Language";
    String bookPublisher1 = "Prentice Hall";
    
    @Test public void testFillUnexpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\"," +
                       "\"author\":KnR}";
        System.out.println(json);
        Book post = new Book(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, post.getTitle());
        assertEquals(bookPublisher1, post.getPublisher());
        assertNotNull(post.getAuthor());
        assertEquals("KnR", post.getAuthor().getID());
        assertNull(post.getAuthor().getName());

    }

    @Test public void testFillExpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\", " +
                       "\"author\":{\"author_id\":\"KnR\", " +
                                     "\"name\":\"Kernighan and Ritchie\"}}";
        Book post = new Book(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, post.getTitle());
        assertEquals(bookPublisher1, post.getPublisher());
        assertNotNull(post.getAuthor());
        assertEquals("KnR", post.getAuthor().getID());
        assertEquals("Kernighan and Ritchie", post.getAuthor().getName());
    }

    /* Online */

    String bookName2 = "yet another book!";
    String bookPublisher2 = "no one";
    
    @Test public void testSaveToServer()  throws Exception {
        final Book book = new Book(stackmob);
        book.setTitle(bookName2);
        book.setPublisher(bookPublisher2);
        
        assertNull(book.getID());

        final MultiThreadAsserter asserter = new MultiThreadAsserter();
        final CountDownLatch latch = latchOne();

        book.createOnServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(book.getID());
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });

        asserter.assertLatchFinished(latch);
    }

    @Test public void testUpdateFromServer() throws Exception {
        final MultiThreadAsserter asserter = new MultiThreadAsserter();
        final CountDownLatch latch = latchOne();
        final Book book = new Book(stackmob);
        book.setID("4f511b979ffcad4fd0034c30");
        book.loadFromServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                System.out.println("success");
                asserter.markEquals(book.getTitle(), bookName2);
                asserter.markEquals(book.getPublisher(), bookPublisher2);
                latch.countDown();

            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });

        asserter.assertLatchFinished(latch);
    }


    public void demoTest() throws Exception {
        final Author author = new Author(stackmob);
        author.setName("Larry Wall");
        author.createOnServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                createBook(author);
            }

            @Override
            public void failure(StackMobException e) {
                System.out.println("failure");
                e.printStackTrace();
            }
        });
        
        System.in.read();
    }
    
    
    public void createBook(Author author) {
        final Book book = new Book(stackmob);
        book.setID("camelbook");
        book.setTitle("Programming Perl");
        book.setPublisher("O'Reilly");
        book.setAuthor(author);
        book.createOnServer( new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                fetchBook();
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void fetchBook() {
        final Book book = new Book(stackmob);
        book.setID("camelbook");
        book.loadFromServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                boolean bookHasData = book.hasData();
                boolean authorHashData = book.getAuthor().hasData();
                fetchBookWithExpand();
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void fetchBookWithExpand() {
        final Book book = new Book(stackmob);
        book.setID("camelbook"); 
        book.loadFromServer(2, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                boolean bookHasData = book.hasData();
                boolean authorHashData = book.getAuthor().hasData();
                System.out.println("success");
                updateBook(book);
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void updateBook(Book book) {
        final Book theBook = book;
        book.setTitle("Programming Perl 2: Perl Harder");
        book.saveOnServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                deleteBook(theBook);
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void deleteBook(Book book) {
        book.deleteFromServer(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                System.out.println("success");
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
            }
        });
    }
}
