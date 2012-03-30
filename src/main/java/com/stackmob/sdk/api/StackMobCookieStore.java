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

import com.stackmob.sdk.util.Pair;
import org.scribe.model.Response;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StackMobCookieStore {

    protected static final String SetCookieHeaderKey = "Set-Cookie";
    protected static final DateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");
    protected static final String EXPIRES = "Expires";

    protected final ConcurrentHashMap<String, Map.Entry<String, Date>> cookies = new ConcurrentHashMap<String, Map.Entry<String, Date>>();


    public void storeCookies(Response resp) {
        storeCookie(resp.getHeaders().get(SetCookieHeaderKey));
    }
    
    protected void storeCookie(String cookieString) {
        addToCookieMap(cookies, cookieString);
    }
    
    protected void addToCookieMap(Map<String,Map.Entry<String,Date>> map, String cookieString) {
        if(cookieString != null) {
            String[] valSplit = cookieString.split(";");
            if (valSplit.length == 1) {
                //cookie only
                String[] cookieSplit = cookieString.split("=");
                if (cookieSplit.length == 2) {
                    map.put(valSplit[0], new Pair<String, Date>( valSplit[1], null));
                }
            } else {
                //cookie and expires
                String[] cookieSplit = valSplit[0].split("=");
                String[] expiresSplit = valSplit[1].split("=");
                Date expires = null;
                if (expiresSplit.length == 2 && cookieSplit.length == 2) {
                    if (expiresSplit[0].equals(EXPIRES)) {
                        try {
                            expires = cookieDateFormat.parse(expiresSplit[1]);
                        } catch (ParseException e) {
                            //do nothing
                        }
                    }
                    map.put(cookieSplit[0], new Pair<String, Date>( cookieSplit[1], expires));
                }
            }
        }
    }
    
    protected String cookieMapToHeaderString(Map<String,Map.Entry<String,Date>> map) {
        //build cookie header
        StringBuilder cookieBuilder = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, Map.Entry<String, Date>> c : cookies.entrySet()) {
            //only use unexpired cookies
            if (isUnexpired(c.getValue())) {
                if(!first) {
                    cookieBuilder.append("; ");
                }
                first = false;
                cookieBuilder.append(c.getKey()).append("=").append(c.getValue().getKey());
            }
        }
        return cookieBuilder.toString();
    }

    private boolean isUnexpired(Map.Entry<String, Date> values) {
        Date expires = values.getValue();
        return expires == null || new Date().compareTo(expires) == 1;
    }

    public void clear() {
        cookies.clear();
    }

    public String cookieHeader() {
        return cookieMapToHeaderString(cookies);
    }
}

