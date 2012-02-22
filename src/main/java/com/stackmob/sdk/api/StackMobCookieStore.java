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

import org.scribe.model.Response;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StackMobCookieStore {

    protected static final String SetCookieHeaderKey = "Set-Cookie";
    protected static final DateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");
    protected static final String EXPIRES = "Expires";

    protected final ConcurrentHashMap<String, Map.Entry<String, Date>> cookies = new ConcurrentHashMap<String, Map.Entry<String, Date>>();


    public void storeCookies(Response resp) {
        String val = resp.getHeaders().get(SetCookieHeaderKey);
        if(val != null) {
            String[] valSplit = val.split(";");
            if (valSplit.length == 1) {
                //cookie only
                String[] cookieSplit = val.split("=");
                if (cookieSplit.length == 2) {
                    storeCookie(valSplit[0], valSplit[1], null);
                }
            }
            else if(valSplit.length == 2) {
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
                    storeCookie(cookieSplit[0], cookieSplit[1], expires);
                }
            }
        }
    }

    protected void storeCookie(String key, String value, Date expiry) {
        cookies.put(key, new AbstractMap.SimpleEntry<String, Date>(value, expiry));
    }

    public String getCookie(String key) {
        String cookie = null;
        Map.Entry<String, Date> cookieVals = cookies.get(key);
        if(cookieVals != null && isUnexpired(cookieVals)) {
            cookie = cookieVals.getKey();
        }
        return cookie;
    }

    private boolean isUnexpired(Map.Entry<String, Date> values) {
        Date expires = values.getValue();
        return expires == null || new Date().compareTo(expires) == 1;
    }

    public void clear() {
        cookies.clear();
    }

    public String cookieHeader() {
        //build cookie header
        StringBuilder cookieBuilder = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, Map.Entry<String, Date>> c : cookies.entrySet()) {
            if(!first) {
                cookieBuilder.append("; ");
            }
            first = false;
            if (isUnexpired(c.getValue())) {
                //only use unexpired cookies
                cookieBuilder.append(c.getKey()).append("=").append(c.getValue().getKey());
            }
        }
        return cookieBuilder.toString();
    }
}

