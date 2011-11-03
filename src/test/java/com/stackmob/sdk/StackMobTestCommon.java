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

import com.google.gson.Gson;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobRedirectedCallback;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.testobjects.Error;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.reflect.TypeToken;
import com.stackmob.sdk.testobjects.StackMobObject;
import com.stackmob.sdk.testobjects.StackMobObjectOnServer;

import static org.junit.Assert.*;

public class StackMobTestCommon {

    private static final String ENVIRONMENT_KEY_KEY = "STACKMOB_KEY";
    private static final String ENVIRONMENT_SECRET_KEY = "STACKMOB_SECRET";

    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";//do not change this
    private static final String DEFAULT_API_SECRET = "DEFAULT_API_SECRET";//do not change this

    public static final String API_KEY = DEFAULT_API_KEY;
    public static final String API_SECRET = DEFAULT_API_SECRET;
    private static final String API_URL_FORMAT = "api.mob1.stackmob.com";
    private static final String PUSH_URL_FORMAT = "push.mob1.stackmob.com";
    public static final String USER_OBJECT_NAME = "user";
    public static final Integer API_VERSION_NUM = 0;

    protected static final Gson gson = new Gson();
    protected final StackMob stackmob;

    public StackMobTestCommon() {
        String apiKey = API_KEY;
        String apiSecret = API_SECRET;

        String envKey = System.getenv(ENVIRONMENT_KEY_KEY);
        String envSecret = System.getenv(ENVIRONMENT_SECRET_KEY);
        if(envKey != null && envSecret != null) {
            System.out.println("found environment vars for key & secret. using these");
            apiKey = envKey;
            apiSecret = envSecret;
        }
        String vmKey = System.getProperty(ENVIRONMENT_KEY_KEY);
        String vmSecret = System.getProperty(ENVIRONMENT_SECRET_KEY);
        if(vmKey != null && vmSecret != null) {
            System.out.println("found JVM args for key & secret. using these & overriding previous");
            apiKey = vmKey;
            apiSecret = vmSecret;
        }

        assertFalse("you forgot to set your API key", DEFAULT_API_KEY.equals(apiKey));
        assertFalse("you forgot to set your API secret", DEFAULT_API_SECRET.equals(apiSecret));
        stackmob = new StackMob(apiKey, apiSecret, USER_OBJECT_NAME, API_VERSION_NUM, API_URL_FORMAT, PUSH_URL_FORMAT, new StackMobRedirectedCallback() {
            @Override public void redirected(String originalUrl, Map<String, String> redirectHeaders, String redirectBody, String newURL) {
                //do nothing
            }
        });
    }

    public static void assertNotError(String responseBody) {
        try {
            Error err = gson.fromJson(responseBody, Error.class);
            assertNull("request failed with error: " + err.error, err.error);
        }
        catch (Exception e) {
            //do nothing
        }
    }

    public static boolean isError(String responseBody) {
        Error err = gson.fromJson(responseBody, Error.class);
        return err.error != null;
    }

    protected String getRandomString() {
        return UUID.randomUUID().toString();
    }

    protected <T extends StackMobObject> StackMobObjectOnServer<T> createOnServer(final T obj, final Class<T> cls) throws StackMobException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        final AtomicReference<String> ref = new AtomicReference<String>(null);

        stackmob.post(obj.getName(), obj, new StackMobCallback() {
            @Override public void success(String responseBody) {
                if(!asserter.markNotJsonError(responseBody)) {
                    try {
                        T obj = gson.fromJson(responseBody, cls);
                        String idField = obj.getIdField();
                        ref.set(idField);
                    }
                    catch(Throwable e) {
                        asserter.markException(e);
                    }
                }
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });

        asserter.assertLatchFinished(latch);
        return new StackMobObjectOnServer<T>(stackmob, ref.get(), obj);
    }
}
