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

import com.google.gson.*;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private transient List<String> relationFields;

    public StackMobModel(String id, StackMob stackmob, Class<? extends StackMobModel> actualClass) {
        this(stackmob,actualClass);
        this.id = id;
    }
    public StackMobModel(StackMob stackmob, Class<? extends StackMobModel> actualClass) {
        this.stackmob = stackmob;
        this.actualClass = actualClass;
        schemaName = actualClass.getSimpleName().toLowerCase();
        relationFields = new ArrayList<String>();
        for(Field field : actualClass.getDeclaredFields()) {
            if(StackMobModel.class.isAssignableFrom(field.getType())) {
                relationFields.add(field.getName());
            }
        }
    }
    
    public void setID(String id) {
        this.id = id;
    }
    
    public String getID() {
        return id;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getIDFieldName() {
        return schemaName +"_id";
    }

    private void fillFieldFromJSON(String fieldName, JsonElement json) {
        try {
            if(fieldName.equals(getIDFieldName())) {
                setID(json.getAsJsonPrimitive().getAsString());
            } else {
                Field field = actualClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                if(relationFields.contains(fieldName)) {
                    StackMobModel relatedModel = (StackMobModel) field.getType().getConstructor(StackMob.class).newInstance(stackmob);
                    relatedModel.fillFromJSON(json);
                    field.set(this, relatedModel);
                } else {
                    if(json.isJsonPrimitive()) {
                        JsonPrimitive primitive = json.getAsJsonPrimitive();
                        if(primitive.isString()) {
                            String prim = primitive.getAsString();
                            field.set(this, prim);
                        }
                            //TODO handle other primitives
                        else if(primitive.isNumber()) field.setInt(this, primitive.getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fillFromJSON(JsonElement json) {
        if(json.isJsonPrimitive()) {
            //This ought to be an unexpanded relation then
            setID(json.getAsJsonPrimitive().getAsString());
        } else {
            for (Map.Entry<String, JsonElement> jsonField : json.getAsJsonObject().entrySet()) {
                fillFieldFromJSON(jsonField.getKey(), jsonField.getValue());
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
        stackmob.get(getSchemaName() + "/" + id, new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                StackMobModel.this.fillFromJSON(new JsonParser().parse(responseBody));
                furtherCallback.success(responseBody);
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }

    public void createOnServer(StackMobCallback callback) {
        stackmob.post(getSchemaName(), toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                fillFromJSON(new JsonParser().parse(responseBody));
                furtherCallback.success(responseBody);
            }

            @Override
            public void failure(StackMobException e) {
                furtherCallback.failure(e);
            }
        });
    }




}
