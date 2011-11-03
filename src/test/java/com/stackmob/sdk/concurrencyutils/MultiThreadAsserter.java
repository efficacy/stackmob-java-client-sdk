package com.stackmob.sdk.concurrencyutils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.testobjects.Error;
import com.stackmob.sdk.util.Pair;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

public class MultiThreadAsserter {
    private final Object mutex = new Object();
    private AtomicBoolean bool = new AtomicBoolean(false);
    private AtomicReference<StackMobException> exception = new AtomicReference<StackMobException>(null);
    private Gson gson = new Gson();

    private void setException(String s) {
        setException(new StackMobException(s));
    }

    private void setException(StackMobException s) {
        synchronized(mutex) {
            if(!bool.get()) {
                bool.set(true);
                exception.set(s);
            }
        }

    }

    public <T> boolean markNotNull(T t) {
        if(t == null) {
            setException(t + " was null");
            return true;
        }
        return false;
    }
    public <T> boolean markNotNull(List<T> lst) {
        if(lst == null) {
            setException(lst + " was null");
            return true;
        }
        return false;
    }

    public boolean markFalse(boolean bool) {
        if(bool) {
            setException(bool + " was supposed to be false");
            return true;
        }
        return false;
    }

    public boolean markTrue(boolean bool) {
        return markFalse(!bool);
    }

    public boolean markEquals(int i1, int i2) {
        if(i1 != i2) {
            setException(i1 + " was not equal to " + i2);
            return true;
        }
        return false;
    }

    public boolean markEquals(String s1, String s2) {
        if(!s1.equals(s2)) {
            setException(s1 + " was not equal to " + s2);
            return true;
        }
        return false;
    }

    public void markException(StackMobException e) {
        setException(e);
    }

    public void markFailure(String s) {
        markException(new StackMobException(s));
    }

    public boolean markNotJsonError(String json) {
        try {
            Error err = gson.fromJson(json, Error.class);
            if(err != null && err.error != null) {
                setException("request failed with error: " + err.error);
                return true;
            }
            return false;
        }
        catch(JsonParseException e) {
            return false;
        }
        catch(NullPointerException e) {
            return false;
        }
    }

    public boolean markJsonError(String json) {
        Error err = gson.fromJson(json, Error.class);
        if(err.error == null) {
            setException("expected a json error, instead got " + json);
            return true;
        }
        return false;
    }

    public boolean markLatchFinished(CountDownLatch latch) throws InterruptedException {
        if(!CountDownLatchUtils.wasLatchFinished(latch)) {
            setException(latch + " did not finish");
            return false;
        }
        return true;
    }

    private void throwIfException() throws StackMobException {
        if(bool.get()) throw exception.get();
    }

    public void assertLatchFinished(CountDownLatch latch) throws StackMobException, InterruptedException {
        throwIfException();
        CountDownLatchUtils.assertLatchFinished(latch, bool, exception);
        throwIfException();
    }

    public void assertLatchFinished(CountDownLatch latch, Pair<Long, TimeUnit> waitTime) throws StackMobException, InterruptedException {
        throwIfException();
        CountDownLatchUtils.assertLatchFinished(latch, waitTime, bool, exception);
        throwIfException();
    }
}
