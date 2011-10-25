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
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.testobjects.Error;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.reflect.TypeToken;
import com.stackmob.sdk.testobjects.StackMobObject;
import com.stackmob.sdk.util.Pair;
import com.stackmob.sdk.testobjects.StackMobObjectOnServer;
import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.*;

import static org.junit.Assert.*;

public class StackMobTestCommon {

    public static final String API_KEY = "8bce5b97-6018-4993-a690-4cc034aa2bfe";
    public static final String API_SECRET = "c2227f24-7ad5-452f-8669-4a4a454c8fe4";

    public static final String USER_OBJECT_NAME = "user";
    public static final Integer API_VERSION_NUM = 0;

    protected static final Gson gson = new Gson();
    protected final StackMob stackmob;

    private static final Type hashMapStringStringType = new TypeToken<HashMap<String, String>>() {}.getType();
    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";
    private static final String DEFAULT_API_SECRET = "DEFAULT_API_SECRET";

    public StackMobTestCommon() {
        assertFalse("you forgot to set your API key", DEFAULT_API_KEY.equals(API_KEY));
        assertFalse("you forgot to set your API secret", DEFAULT_API_SECRET.equals(API_SECRET));
        stackmob = new StackMob(API_KEY, API_SECRET, USER_OBJECT_NAME, API_VERSION_NUM);
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

    private Pair<Boolean, String> getErrorFromJson(String json) {
        HashMap<String, String> hm = getHashMapFromJson(json);
        if(hm.get("error") != null) {
            return new Pair<Boolean, String>(true, hm.get("error"));
        }
        return new Pair<Boolean, String>(false, "");
    }

    private HashMap<String, String> getHashMapFromJson(String json) {
        return gson.fromJson(json, hashMapStringStringType);
    }


    protected StackMobObjectOnServer createOnServer(final StackMobObject obj) throws StackMobException, InterruptedException {
        final AtomicBoolean errorBool = new AtomicBoolean();
        final AtomicReference<StackMobException> exception = new AtomicReference<StackMobException>(null);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> ref = new AtomicReference<String>(null);

        stackmob.post(obj.getName(), this, new StackMobCallback() {
            @Override public void success(String responseBody) {
                Pair<Boolean, String> err = getErrorFromJson(responseBody);
                if(err.getFirst()) {
                    errorBool.set(true);
                    exception.set(new StackMobException(err.getSecond()));
                }
                else {
                    HashMap<String, String> hm = getHashMapFromJson(responseBody);
                    ref.set(hm.get(obj.getIdFieldName()));
                }
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                errorBool.set(true);
                exception.set(e);
                latch.countDown();
            }
        });

        assertLatchFinished(latch, errorBool, exception);
        return new StackMobObjectOnServer(stackmob, ref.get(), obj);
    }
}
