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