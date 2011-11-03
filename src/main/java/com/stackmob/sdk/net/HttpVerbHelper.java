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

package com.stackmob.sdk.net;

import com.stackmob.sdk.exception.StackMobException;

import static com.stackmob.sdk.net.HttpVerbWithPayload.POST;
import static com.stackmob.sdk.net.HttpVerbWithPayload.PUT;
import static com.stackmob.sdk.net.HttpVerbWithoutPayload.DELETE;
import static com.stackmob.sdk.net.HttpVerbWithoutPayload.GET;

public abstract class HttpVerbHelper {
    public static HttpVerb valueOf(String s) throws StackMobException {
        if(s.equalsIgnoreCase("get")) {
            return GET;
        }
        else if(s.equalsIgnoreCase("post")) {
            return POST;
        }
        else if(s.equalsIgnoreCase("put")) {
            return PUT;
        }
        else if(s.equalsIgnoreCase("delete")) {
            return DELETE;
        }
        else {
            throw new StackMobException("unknown HTTP verb " + s);
        }
    }
}