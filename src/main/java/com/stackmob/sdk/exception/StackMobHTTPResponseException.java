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

package com.stackmob.sdk.exception;

import com.stackmob.sdk.util.ListHelpers;

import java.util.List;
import java.util.Map;

public class StackMobHTTPResponseException extends StackMobException {
    private Integer code;
    private List<Map.Entry<String, String>> headers;
    private byte[] body;

    public StackMobHTTPResponseException(Integer code, List<Map.Entry<String, String>> headers, byte[] body) {
        super(String.format("call failed with HTTP response code %s, headers %s, body %s", code.toString(), ListHelpers.join(headers, ", "), new String(body)));
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public Integer getCode() {
        return code;
    }
    
    public List<Map.Entry<String, String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
