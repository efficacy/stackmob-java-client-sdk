package com.stackmob.sdk.api;

import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobRedirectedCallback;
import com.stackmob.sdk.net.HttpVerbWithPayload;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class StackMobRequestWithPayload extends StackMobRequest {
    private Object requestObject;
    public StackMobRequestWithPayload(ExecutorService executor,
                                      StackMobSession session,
                                      HttpVerbWithPayload verb,
                                      Map<String, String> headers,
                                      Map<String, String> params,
                                      Object requestObject,
                                      String method,
                                      StackMobCallback cb,
                                      StackMobRedirectedCallback redirCb) {
        super(executor, session, verb, headers, params, method, cb, redirCb);
        this.requestObject = requestObject;
    }

    public StackMobRequestWithPayload(ExecutorService executor, StackMobSession session, HttpVerbWithPayload verb, String method, StackMobCallback cb, StackMobRedirectedCallback redirCb) {
        this(executor, session, verb, StackMobRequest.EmptyHeaders, StackMobRequest.EmptyParams, null, method, cb, redirCb);
    }

    @Override protected String getRequestBody() {
        if(this.requestObject != null) {
            return gson.toJson(this.requestObject);
        }
        return "";
    }
}
