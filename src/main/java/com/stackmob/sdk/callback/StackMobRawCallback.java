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

package com.stackmob.sdk.callback;

import com.stackmob.sdk.net.HttpVerb;

import java.util.List;
import java.util.Map;

public interface StackMobRawCallback {
    /**
     * the method that will be called when the call to StackMob is complete. may be executed in a background thread
     * @param requestVerb the HTTP verb that was requested
     * @param requestURL the URL that was requested
     * @param requestHeaders the headers in the request
     * @param requestBody the body of the request. will be an empty string for GET, DELETE, etc...
     * @param responseStatusCode the status code of the HTTP response from StackMob
     * @param responseHeaders the response headers from StackMob
     * @param responseBody the response body from StackMob
     */
    void done(HttpVerb requestVerb,
              String requestURL,
              List<Map.Entry<String, String>> requestHeaders,
              String requestBody,
              Integer responseStatusCode,
              List<Map.Entry<String, String>> responseHeaders,
              byte[] responseBody);
}
