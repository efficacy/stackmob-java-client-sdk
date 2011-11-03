package com.stackmob.sdk.api;

import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobRedirectedCallback;
import com.stackmob.sdk.net.HttpVerbWithoutPayload;

import java.util.concurrent.ExecutorService;
import java.util.Map;

public class StackMobRequestWithoutPayload extends StackMobRequest {
    public StackMobRequestWithoutPayload(ExecutorService executor,
                                         StackMobSession session,
                                         HttpVerbWithoutPayload verb,
                                         Map<String, String> headers,
                                         Map<String, String> params,
                                         String method,
                                         StackMobCallback cb,
                                         StackMobRedirectedCallback redirCb) {
        super(executor, session, verb, headers, params, method, cb, redirCb);
    }

    public StackMobRequestWithoutPayload(ExecutorService executor, StackMobSession session, HttpVerbWithoutPayload verb, String method, StackMobCallback cb, StackMobRedirectedCallback redirCb) {
        this(executor, session, verb, StackMobRequest.EmptyHeaders, StackMobRequest.EmptyParams, method, cb, redirCb);
    }

    @Override protected String getRequestBody() {
        return "";
    }
}