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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class StackMobRawCallback {
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
    public abstract void done(HttpVerb requestVerb,
                              String requestURL,
                              List<Map.Entry<String, String>> requestHeaders,
                              String requestBody,
                              Integer responseStatusCode,
                              List<Map.Entry<String, String>> responseHeaders,
                              byte[] responseBody);

    /**
     * get the total number of items from the Content-Range header
     * @param responseHeaders the headers that were returned in the response
     * @return the total number of items returned in the Content-Range header, -1 if there was no Content-Range header
     * or it was malformed, -2 if the Content-Length header was present and well formed but the instance length was "*"
     */
    public static Integer getTotalNumberOfItemsFromContentRange(List<Map.Entry<String, String>> responseHeaders) {
        Map.Entry<String, String> contentLengthHeader = null;

        for(Map.Entry<String, String> header: responseHeaders) {
            if(header.getKey().toLowerCase().equals("content-length")) {
                contentLengthHeader = header;
            }
        }

        if(contentLengthHeader != null) {
            List<String> hyphenSplit = Arrays.asList(contentLengthHeader.getValue().split("\\-"));
            if(hyphenSplit.size() != 2) {
                return -1;
            }
            List<String> slashSplit = Arrays.asList(hyphenSplit.get(1).split("/"));
            if(slashSplit.size() != 2) {
                return -1;
            }
            String instanceLengthString = slashSplit.get(1).trim();
            if(instanceLengthString.equals("*")) {
                return -2;
            }
            try {
                return Integer.parseInt(instanceLengthString);
            }
            catch(Throwable t) {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
}
