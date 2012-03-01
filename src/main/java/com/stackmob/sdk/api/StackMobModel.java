package com.stackmob.sdk.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: drapp
 * Date: 3/1/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StackMobModel {

    private abstract class StackMobIntermediaryCallback extends StackMobCallback {
        StackMobCallback furtherCallback;
        public StackMobIntermediaryCallback(StackMobCallback furtherCallback) {
            this.furtherCallback = furtherCallback;
        }
    }
    
    private transient String id;
    private transient StackMob stackmob;
    private transient Class<? extends StackMobModel> actualClass;
    private transient String schemaName;

    public StackMobModel(String id, StackMob stackmob, Class<? extends StackMobModel> actualClass) {
        this.id = id;
        this.stackmob = stackmob;
        this.actualClass = actualClass;
        schemaName = actualClass.getSimpleName().toLowerCase();
    }
    
    protected String getIDFieldName() {
        return schemaName +"_id";
    }

    public void initFromJSON(String json) {
        JsonObject topLevel = new JsonParser().parse(json).getAsJsonObject();
        for (Map.Entry<String, JsonElement> jsonField : topLevel.entrySet()) {
            try {
                String fieldName = jsonField.getKey();
                Field field = actualClass.getDeclaredField(fieldName);
                field.set(this, jsonField.getValue().getAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public String toJSON() {
        JsonObject json = new Gson().toJsonTree(this).getAsJsonObject();
        if(id != null) {
            json.addProperty(getIDFieldName(),id);
        }
        return json.toString();
    }
    
    public void fetch(StackMobCallback callback) {
        stackmob.get(schemaName + "/" + id, new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                StackMobModel.this.initFromJSON(responseBody);
                furtherCallback.success(responseBody);
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }

    public void init(StackMobCallback callback) {

        stackmob.post(schemaName, toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                initFromJSON(responseBody);
                furtherCallback.success(responseBody);
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }



}
