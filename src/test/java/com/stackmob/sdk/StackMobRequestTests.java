/**
 * Copyright 2011 StackMob
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

import com.stackmob.sdk.api.*;
import com.stackmob.sdk.callback.StackMobRedirectedCallback;
import com.stackmob.sdk.net.*;
import com.stackmob.sdk.testobjects.Error;
import org.junit.Test;
import static org.junit.Assert.*;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StackMobRequestTests extends StackMobTestCommon {
    private StackMobRedirectedCallback redirectedCallback = new StackMobRedirectedCallback() {
      @Override
      public void redirected(String originalUrl, Map<String, String> redirectHeaders, String redirectBody, String newURL) {
        //do nothing
      }
    };

    private StackMobSession session = stackmob.getSession();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Test public void testListapiSecureGetRequest() {
        StackMobRequest request = new StackMobRequestWithoutPayload(executor, session, HttpVerbWithoutPayload.GET, "listapi", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                assertNotNull(responseBody);
            }
            @Override
            public void failure(StackMobException e) {
                fail(e.getMessage());
            }
        }, redirectedCallback);

        request.sendRequest();
    }

    @Test public void testListapiSecurePostRequest() {
        StackMobRequest request = new StackMobRequestWithPayload(executor, session, HttpVerbWithPayload.POST, "listapi", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                assertNotNull(responseBody);
            }
            @Override
            public void failure(StackMobException e) {
                fail(e.getMessage());
            }
        }, redirectedCallback);

        request.sendRequest();
    }

    @Test
    public void testListapiRegularGetRequest() {
        StackMobRequest request = new StackMobRequestWithoutPayload(executor, session, HttpVerbWithoutPayload.GET, "listapi", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                assertNotNull(responseBody);
            }
            @Override
            public void failure(StackMobException e) {
                fail(e.getMessage());
            }
        }, redirectedCallback);

        request.sendRequest();
    }

    @Test
    public void testListapiRegularPostRequest() {
        StackMobRequest request = new StackMobRequestWithPayload(executor, session, HttpVerbWithPayload.POST, "listapi", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                assertNotNull(responseBody);
            }
            @Override
            public void failure(StackMobException e) {
                fail(e.getMessage());
            }
        }, redirectedCallback);

        request.sendRequest();
    }

    @Test
    public void testInexistentMethodShouldFail() {
        StackMobRequest request = new StackMobRequestWithoutPayload(executor, session, HttpVerbWithoutPayload.GET, "nonexistent", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                Error err = gson.fromJson(responseBody, Error.class);
                assertNotNull(err.error);
            }
            @Override
            public void failure(StackMobException e) {
                assertNotNull(e.getMessage());
            }
        }, redirectedCallback);

        request.sendRequest();
    }
    
    @Test
    public void testRequestShouldReturnSendSuccess() {
        StackMobRequest request = new StackMobRequestWithoutPayload(executor, session, HttpVerbWithoutPayload.GET, "listapi", new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                assertNotNull(responseBody);
            }

            @Override
            public void failure(StackMobException e) {
                fail(e.getMessage());
            }
        }, redirectedCallback);
        StackMobRequestSendResult sendResult = request.setUrlFormat("nonexistent").sendRequest();
        assertEquals(sendResult.getStatus(), StackMobRequestSendResult.RequestSendStatus.SENT);
        assertNull(sendResult.getFailureReason());
    }
}