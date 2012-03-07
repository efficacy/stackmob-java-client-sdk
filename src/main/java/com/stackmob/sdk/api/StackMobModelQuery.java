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
package com.stackmob.sdk.api;

import com.google.gson.*;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StackMobModelQuery<T extends StackMobModel>{

    Class<T> classOfT;
    StackMobQuery query;

    public StackMobModelQuery(Class<T> classOfT) {
        this.classOfT = classOfT;
        this.query = new StackMobQuery(this.classOfT.getSimpleName().toLowerCase());
    }
    
    public StackMobQuery getQuery() {
        return query;
    }

    public void send(StackMobQueryCallback<T> callback) {
        final StackMobQueryCallback<T> furtherCallback = callback;
        StackMob.getStackMob().get(query, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                JsonArray array = new JsonParser().parse(responseBody).getAsJsonArray();
                List<T> resultList = new ArrayList<T>();
                Iterator<JsonElement> it = array.iterator();
                while(it.hasNext()) {
                    resultList.add(new Gson().fromJson(it.next(), classOfT));
                }
                furtherCallback.success(resultList);
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }

    /*
    public void send(StackMobQueryCallback<T> callback) {
        final StackMobQueryCallback<T> furtherCallback = callback;
        StackMob.getStackMob().get(this, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                furtherCallback.success((List<T>)new Gson().fromJson(responseBody,responseType));
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }
    */
}
