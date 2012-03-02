package com.stackmob.sdk;

import com.google.gson.JsonParser;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.testobjects.Book;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: drapp
 * Date: 3/1/12
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
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
                   "\"publisher\":\"" + bookPublisher1 +"\", " +
                       "\"owner\":\"bob\"}";
        Book post = new Book(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, post.getTitle());
        assertEquals(bookPublisher1, post.getPublisher());
        assertNotNull(post.getOwner());
        assertEquals("bob", post.getOwner().getID());

    }

    @Test public void testFillExpandedJSON() throws Exception {
        String json = "{\"title\":\"" + bookName1 + "\"," +
                   "\"publisher\":\"" + bookPublisher1 +"\", " +
                       "\"owner\":{\"username\":\"bob\", " +
                                  "\"password\":\"hunter2\", " +
                                     "\"email\":\"test@test.com\"}}";
        Book post = new Book(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        assertEquals(bookName1, post.getTitle());
        assertEquals(bookPublisher1, post.getPublisher());
        assertNotNull(post.getOwner());
        assertEquals("bob", post.getOwner().getID());
        assertEquals("hunter2", post.getOwner().getPassword());
        assertEquals("test@test.com", post.getOwner().getEmail());
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
        book.fetch( new StackMobCallback() {
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
}
