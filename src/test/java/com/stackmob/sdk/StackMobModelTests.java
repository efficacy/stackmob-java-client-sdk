package com.stackmob.sdk;

import com.google.gson.JsonParser;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.testobjects.BlogPost;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: drapp
 * Date: 3/1/12
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackMobModelTests extends StackMobTestCommon {

    MultiThreadAsserter asserter = new MultiThreadAsserter();

    @Test public void testFillUnexpandedJSON() throws Exception {
        String json = "{\"title\":\"Hello World!\"," +
                        "\"post\":\"this is json\", " +
                       "\"owner\":\"bob\"}";
        BlogPost post = new BlogPost(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        asserter.markTrue("Hello World!".equals(post.title));
        asserter.markTrue("this is json".equals(post.post));
        asserter.markNotNull(post.owner);
        asserter.markTrue("bob".equals(post.owner.getID()));

    }

    @Test public void testFillExpandedJSON() throws Exception {
        String json = "{\"title\":\"Hello World!\", " +
                        "\"post\":\"this is json\", " +
                       "\"owner\":{\"username\":\"bob\", " +
                                  "\"password\":\"hunter2\", " +
                                     "\"email\":\"test@test.com\"}}";
        BlogPost post = new BlogPost(stackmob);
        post.fillFromJSON(new JsonParser().parse(json));
        asserter.markTrue("Hello World!".equals(post.title));
        asserter.markTrue("this is json".equals(post.post));
        asserter.markNotNull(post.owner);
        asserter.markTrue("bob".equals(post.owner.getID()));
        asserter.markTrue("hunter2".equals(post.owner.getPassword()));
        asserter.markTrue("test@test.com".equals(post.owner.getEmail()));
    }
}
