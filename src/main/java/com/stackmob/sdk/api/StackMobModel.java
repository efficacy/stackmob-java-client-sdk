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

import java.lang.reflect.Field;
import java.util.*;

public abstract class StackMobModel {

    private class StackMobIntermediaryCallback extends StackMobCallback {
        StackMobCallback furtherCallback;
        public StackMobIntermediaryCallback(StackMobCallback furtherCallback) {
            this.furtherCallback = furtherCallback;
        }

        @Override
        public void success(String responseBody) {
            furtherCallback.success(responseBody);
        }

        @Override
        public void failure(StackMobException e) {
            furtherCallback.failure(e);
        }
    }

    private enum FieldGroup {
        PRIMITIVE,
        OBJECT,
        MODEL,
        OBJECT_ARRAY,
        MODEL_ARRAY
    }

    private static Map<String,FieldGroup> fieldGroups;
    
    private transient String id;
    private transient StackMob stackmob;
    private transient Class<? extends StackMobModel> actualClass;
    private transient String schemaName;
    private transient boolean hasData;

    public StackMobModel(String id, StackMob stackmob, Class<? extends StackMobModel> actualClass) {
        this(stackmob,actualClass);
        this.id = id;
    }
    public StackMobModel(StackMob stackmob, Class<? extends StackMobModel> actualClass) {
        this.stackmob = stackmob;
        this.actualClass = actualClass;
        schemaName = actualClass.getSimpleName().toLowerCase();
        fieldGroups = new HashMap<String, FieldGroup>();
        hasData = false;
        determineFieldGroups();
    }

    private void determineFieldGroups() {
        //Sort the fields into groupings we care about for serialization
        //TODO: we should only do this once per class. static map class -> fieldGroups?
        for(Field field : actualClass.getDeclaredFields()) {
            fieldGroups.put(field.getName(), determineFieldGroup(field));
        }
    }
    
    private FieldGroup determineFieldGroup(Field field) {
        if(field.getType().isPrimitive()) {
            return FieldGroup.PRIMITIVE;
        } else if(field.getType().isArray()) {
            if(isModel(field)) {
                return FieldGroup.MODEL_ARRAY;
            } else {
                return FieldGroup.OBJECT_ARRAY;
            }
        } else if(isModel(field)) {
            return FieldGroup.MODEL;
        } else {
            return FieldGroup.OBJECT;
    }
}
    
    private static boolean isModel(Field field) {
        return StackMobModel.class.isAssignableFrom(field.getType());
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

    public boolean hasData() {
        return hasData;
    }

    private void fillFieldFromJSON(String fieldName, JsonElement json) {
        try {
            if(fieldName.equals(getIDFieldName())) {
                // The id field is special, its name doesn't match the field
                setID(json.getAsJsonPrimitive().getAsString());
            } else {
                Field field = actualClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                if(fieldGroups.get(fieldName) == FieldGroup.MODEL) {
                    // Delegate any expanded relations to the appropriate object
                    StackMobModel relatedModel = (StackMobModel) field.getType().getConstructor(StackMob.class).newInstance(stackmob);
                    relatedModel.fillFromJSON(json);
                    field.set(this, relatedModel);
                } else {
                    // Let gson do its thing
                    field.set(this, new Gson().fromJson(json,field.getType()));
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
            hasData = true;
        }
    }
    
    private List<String> getFieldNames(JsonObject json) {
        Set<Map.Entry<String,JsonElement>> entrySet = json.entrySet();
        List<String> list = new ArrayList<String>();
        for(Map.Entry<String,JsonElement> entry : json.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }
    
    public String toJSON() {
        JsonObject json = new Gson().toJsonTree(this).getAsJsonObject();
        for(String fieldName : getFieldNames(json)) {
            JsonElement value = json.get(fieldName);
            if(fieldGroups.get(fieldName) == FieldGroup.MODEL) {
                json.remove(fieldName);
                try {
                    Field relationField = actualClass.getDeclaredField(fieldName);
                    relationField.setAccessible(true);
                    StackMobModel relatedModel = (StackMobModel) relationField.get(this);
                    //TODO: expand?
                    json.add(fieldName, new JsonPrimitive(relatedModel.getID()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            } else if(!value.isJsonPrimitive()) {
                String jsonString = value.toString();
                json.remove(fieldName);
                json.add(fieldName, new JsonPrimitive(jsonString));
            }
        }
        if(id != null) {
            json.addProperty(getIDFieldName(),id);
        }
        return json.toString();
    }

    public void loadFromServer(StackMobCallback callback) {
        loadFromServer(1, callback);
    }
    
    public void loadFromServer(int depth, StackMobCallback callback) {
        StackMobQueryWithField q = new StackMobQuery(getSchemaName()).expandDepthIs(depth).field(getIDFieldName()).isEqualTo(id);
        
        Map<String,String> args = new HashMap<String, String>();
        if(depth > 1 ) args.put("_expand", String.valueOf(depth));
        Map<String,String> headers = new HashMap<String, String>();
        //headers.put("X-StackMob-Expand", String.valueOf(depth));
        stackmob.get(getSchemaName() + "/" + id, args, headers , new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                StackMobModel.this.fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }

    public void createOnServer(StackMobCallback callback) {
        stackmob.post(getSchemaName(), toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }

    public void saveOnServer(StackMobCallback callback) {
        stackmob.put(getSchemaName(), id, toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }

    public void deleteFromServer(StackMobCallback callback) {
        stackmob.delete(getSchemaName(), id, callback);
    }




}
