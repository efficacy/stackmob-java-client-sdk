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

package com.stackmob.sdk.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.google.gson.GsonBuilder;
import com.stackmob.sdk.callback.StackMobRedirectedCallback;
import com.stackmob.sdk.net.*;
import com.stackmob.sdk.push.StackMobPushToken;
import com.stackmob.sdk.push.StackMobPushTokenDeserializer;
import com.stackmob.sdk.push.StackMobPushTokenSerializer;
import com.stackmob.sdk.util.Pair;

import com.google.gson.Gson;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;
import org.scribe.model.Response;
import org.scribe.oauth.OAuthService;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.builder.ServiceBuilder;

public abstract class StackMobRequest {

    public static final Map<String, String> EmptyHeaders = new HashMap<String, String>();
    public static final Map<String, String> EmptyParams = new HashMap<String, String>();

    public static final String DEFAULT_URL_FORMAT = "mob1.stackmob.com";
    public static final String DEFAULT_API_URL_FORMAT = "api." + DEFAULT_URL_FORMAT;
    public static final String DEFAULT_PUSH_URL_FORMAT = "push." + DEFAULT_URL_FORMAT;
    protected static final String SECURE_SCHEME = "https";
    protected static final String REGULAR_SCHEME = "http";

    protected final ExecutorService executor;
    protected final StackMobSession session;
    protected StackMobCallback callback;
    protected final StackMobRedirectedCallback redirectedCallback;

    protected HttpVerb httpVerb;
    protected String methodName;

    protected String urlFormat = DEFAULT_API_URL_FORMAT;
    protected Boolean isSecure = false;
    protected Map<String, String> params;
    protected Map<String, String> headers;

    protected Gson gson;

    private OAuthService oAuthService;

    protected StackMobRequest(ExecutorService executor,
                              StackMobSession session,
                              HttpVerb verb,
                              Map<String, String> headers,
                              Map<String, String> params,
                              String method,
                              StackMobCallback cb,
                              StackMobRedirectedCallback redirCb) {
        this.executor = executor;
        this.session = session;
        this.httpVerb = verb;
        this.headers = headers;
        this.params = params;
        this.methodName = method;
        this.callback = cb;
        this.redirectedCallback = redirCb;

        GsonBuilder gsonBuilder = new GsonBuilder()
                                  .registerTypeAdapter(StackMobPushToken.class, new StackMobPushTokenDeserializer())
                                  .registerTypeAdapter(StackMobPushToken.class, new StackMobPushTokenSerializer())
                                  .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.TRANSIENT, Modifier.STATIC);
        gson = gsonBuilder.create();

        oAuthService = new ServiceBuilder().provider(StackMobApi.class).apiKey(session.getKey()).apiSecret(session.getSecret()).build();
    }

    public StackMobRequest setUrlFormat(String urlFmt) {
        this.urlFormat = urlFmt;
        return this;
    }

    protected abstract String getRequestBody();

    public void sendRequest() {
        try {
            if(HttpVerbWithoutPayload.GET == httpVerb) {
                sendGetRequest();
            }
            else if(HttpVerbWithPayload.POST == httpVerb) {
                sendPostRequest();
            }
            else if(HttpVerbWithPayload.PUT == httpVerb) {
                sendPutRequest();
            }
            else if(HttpVerbWithoutPayload.DELETE == httpVerb) {
                sendDeleteRequest();
            }
        }
        catch (StackMobException e) {
            callback.failure(e);
        }
    }

    private void sendGetRequest() throws StackMobException {
        try {
            String query = formatQueryString(this.params);
            URI uri = createURI(getScheme(), urlFormat, getPath(), query);
            OAuthRequest req = getOAuthRequest(HttpVerbWithoutPayload.GET, uri.toString());
            sendRequest(req);
        }
        catch (URISyntaxException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (InterruptedException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (ExecutionException e) {
            throw new StackMobException(e.getMessage());
        }
    }

    private void sendPostRequest() throws StackMobException {
        try {
            URI uri = createURI(getScheme(), urlFormat, getPath(), "");
            String payload = getRequestBody();
            OAuthRequest req = getOAuthRequest(HttpVerbWithPayload.POST, uri.toString(), payload);
            sendRequest(req);
        }
        catch (URISyntaxException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (InterruptedException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (ExecutionException e) {
            throw new StackMobException(e.getMessage());
        }
    }

    private void sendPutRequest() throws StackMobException {
        try {
            URI uri = createURI(getScheme(), urlFormat, getPath(), "");
            String payload = getRequestBody();
            OAuthRequest req = getOAuthRequest(HttpVerbWithPayload.PUT, uri.toString(), payload);
            sendRequest(req);
        }
        catch (URISyntaxException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (InterruptedException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (ExecutionException e) {
            throw new StackMobException(e.getMessage());
        }
    }

    private void sendDeleteRequest() throws StackMobException {
        try {
            String query = formatQueryString(this.params);
            URI uri = createURI(getScheme(), urlFormat, getPath(), query);
            OAuthRequest req = getOAuthRequest(HttpVerbWithoutPayload.DELETE, uri.toString());
            sendRequest(req);
        }
        catch (URISyntaxException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (InterruptedException e) {
            throw new StackMobException(e.getMessage());
        }
        catch (ExecutionException e) {
            throw new StackMobException(e.getMessage());
        }
    }

    private URI createURI(String scheme, String host, String path, String query) throws URISyntaxException {
        StringBuilder uriBuilder = new StringBuilder().append(scheme).append("://").append(host);
        if(!path.startsWith("/")) {
            uriBuilder.append("/");
        }
        uriBuilder.append(path);

        if(query != null && query.length() > 0) {
            uriBuilder.append("?").append(query);
        }

        return new URI(uriBuilder.toString());
    }

    protected String getPath() {
        if(methodName.startsWith("/")) {
            return methodName;
        }
        else {
            return "/" + methodName;
        }
    }

    private String getScheme() {
        if (isSecure) {
            return SECURE_SCHEME;
        }
        else {
            return REGULAR_SCHEME;
        }
    }

    private static String percentEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }

    protected static String formatQueryString(Map<String, String> params) {
        StringBuilder formatBuilder = new StringBuilder();
        boolean first = true;
        for(String key : params.keySet()) {
            if(!first) {
                formatBuilder.append("&");
            }
            first = false;
            String value = params.get(key);
            try {
                formatBuilder.append(percentEncode(key)).append("=").append(percentEncode(value));
            }
            catch(UnsupportedEncodingException e) {
                //do nothing
            }
        }
        return formatBuilder.toString();
    }



    private OAuthRequest getOAuthRequest(HttpVerb method, String url) {
        OAuthRequest oReq = new OAuthRequest(Verb.valueOf(method.toString()), url);
        int apiVersion = session.getApiVersionNumber();
        final String contentType = "application/vnd.stackmob+json;";
        final String accept = contentType + " version="+apiVersion;
        String userAgentIntermediate = "StackMob Java Client; " + apiVersion;
        if(session.getAppName() != null) {
            userAgentIntermediate += "/"+session.getAppName();
        }
        final String userAgent = userAgentIntermediate;

        List<Pair<String, String>> headerList = new ArrayList<Pair<String, String>>();
        if(this.headers != null) {
            for(String name : this.headers.keySet()) {
                headerList.add(new Pair<String, String>(name, this.headers.get(name)));
            }
        }
        headerList.add(new Pair<String, String>("Content-Type", contentType));
        headerList.add(new Pair<String, String>("Accept", accept));
        headerList.add(new Pair<String, String>("User-Agent", userAgent));

        for(Pair<String, String> header: headerList) {
            oReq.addHeader(header.getFirst(), header.getSecond());
        }

        oAuthService.signRequest(new Token("", ""), oReq);
        return oReq;
    }

    private OAuthRequest getOAuthRequest(HttpVerb method, String url, String payload) {
        OAuthRequest req = getOAuthRequest(method, url);
        req.addPayload(payload);
        return req;
    }

    private void sendRequest(final OAuthRequest req) throws InterruptedException, ExecutionException {
        final StackMobCallback cb = this.callback;
        executor.submit(new Callable<Object>() {
            @Override
            public String call() throws Exception {
                Response ret = req.send();
                if(HttpRedirectHelper.isRedirected(ret.getCode())) {
                    try {
                        String newLocation = HttpRedirectHelper.getNewLocation(ret.getHeaders());
                        HttpVerb verb = HttpVerbHelper.valueOf(req.getVerb().toString());
                        OAuthRequest newReq = getOAuthRequest(verb, newLocation);
                        if(req.getBodyContents() != null && req.getBodyContents().length() > 0) {
                            newReq = getOAuthRequest(verb, newLocation, req.getBodyContents());
                        }
                        //does NOT protect against circular redirects
                        redirectedCallback.redirected(req.getUrl(), ret.getHeaders(), ret.getBody(), newReq.getUrl());
                        sendRequest(newReq);
                    }
                    catch(Exception e) {
                        callback.failure(new StackMobException(e.getMessage()));
                    }
                }
                else {
                    cb.success(ret.getBody());
                }
                return null;
            }
        });
    }

}