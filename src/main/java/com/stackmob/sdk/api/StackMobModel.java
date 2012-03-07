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
import com.stackmob.sdk.callback.StackMobIntermediaryCallback;
import com.stackmob.sdk.callback.StackMobNoopCallback;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.util.SerializationMetadata;
import static com.stackmob.sdk.util.SerializationMetadata.*;

import java.lang.reflect.Field;
import java.util.*;

public abstract class StackMobModel {
    
    private transient String id;
    private transient Class<? extends StackMobModel> actualClass;
    private transient String schemaName;
    private transient boolean hasData;
    private transient int depth;

    public StackMobModel(String id, Class<? extends StackMobModel> actualClass) {
        this(actualClass);
        this.id = id;
    }

    public StackMobModel(Class<? extends StackMobModel> actualClass) {
        this.actualClass = actualClass;
        schemaName = actualClass.getSimpleName().toLowerCase();
        ensureValidName(schemaName,"model");
        ensureMetadata(actualClass);
    }

    private static void ensureValidName(String name, String thing) {
        //The three character minimum isn't actually enforced for fields
        if(name.matches(".*(\\W|_).*") || name.length() > 25 || name.length() < 3) {
            throw new IllegalStateException(String.format("Invalid name for a %s: %s. Must be 3-25 alphanumeric characters", thing, name));
        }
    }

    private SerializationMetadata getMetadata(String fieldName) {
        return getSerializationMetadata(actualClass, fieldName);
    }
    
    public void setID(String id) {
        this.id = id;
    }
    
    public String getID() {
        return id;
    }

    /**
     * Determines the schema connected to this class on the server. By
     * default it's the name of the class in lower case. Override in
     * subclasses to change that. Must be 3-25 alphanumeric characters.
     * @return
     */
    protected String getSchemaName() {
        return schemaName;
    }

    public String getIDFieldName() {
        return schemaName +"_id";
    }

    public boolean hasData() {
        return hasData;
    }
    
    public void setDepth(int depth) {
        this.depth = depth;
    }

    protected void fillFieldFromJSON(String fieldName, JsonElement json) {
        try {
            if(fieldName.equals(getIDFieldName())) {
                // The id field is special, its name doesn't match the field
                setID(json.getAsJsonPrimitive().getAsString());
            } else {
                Field field = getField(fieldName);
                field.setAccessible(true);
                if(getMetadata(fieldName) == MODEL) {
                    // Delegate any expanded relations to the appropriate object
                    StackMobModel relatedModel = (StackMobModel) field.getType().newInstance();
                    relatedModel.fillFromJSON(json);
                    field.set(this, relatedModel);
                } else if(getMetadata((fieldName)) == OBJECT) {
                    field.set(this, new Gson().fromJson(json.getAsJsonPrimitive().getAsString(), field.getType()));
                } else {
                    // Let gson do its thing
                    field.set(this, new Gson().fromJson(json,field.getType()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Field getField(String fieldName) throws NoSuchFieldException {
        Class<?> classToCheck = actualClass;
        while(!classToCheck.equals(StackMobModel.class)) {
            try {
                return classToCheck.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) { }
            classToCheck = classToCheck.getSuperclass();
        }
        throw new NoSuchFieldException(fieldName);
    }

    protected void fillFromJSON(JsonElement json) {
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
        List<String> list = new ArrayList<String>();
        for(Map.Entry<String,JsonElement> entry : json.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }
    
    protected String toJSON() {
        JsonObject json = new Gson().toJsonTree(this).getAsJsonObject();
        for(String fieldName : getFieldNames(json)) {
            ensureValidName(fieldName, "field");
            JsonElement value = json.get(fieldName);
            if(getMetadata(fieldName) == MODEL) {
                json.remove(fieldName);
                try {
                    Field relationField = actualClass.getDeclaredField(fieldName);
                    relationField.setAccessible(true);
                    StackMobModel relatedModel = (StackMobModel) relationField.get(this);
                    json.add(fieldName, new JsonPrimitive(relatedModel.getID()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            //TODO: handle array types here
            } else if(getMetadata(fieldName) == OBJECT) {
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
    
    public void init() {
        init(new StackMobNoopCallback());
    }

    public void init(StackMobCallback callback) {
        StackMob.getStackMob().post(getSchemaName(), toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }
    
    public void load() {
        load(new StackMobNoopCallback());
    }
    
    public void load(StackMobCallback callback) {
        Map<String,String> args = new HashMap<String, String>();
        if(depth > 0) args.put("_expand", String.valueOf(depth));
        Map<String,String> headers = new HashMap<String, String>();
        StackMob.getStackMob().get(getSchemaName() + "/" + id, args, headers , new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                StackMobModel.this.fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }
    
    public void save() {
        save(new StackMobNoopCallback());
    }

    public void save(StackMobCallback callback) {
        StackMob.getStackMob().put(getSchemaName(), id, toJSON(), new StackMobIntermediaryCallback(callback) {
            @Override
            public void success(String responseBody) {
                fillFromJSON(new JsonParser().parse(responseBody));
                super.success(responseBody);
            }
        });
    }

    public void delete() {
        delete(new StackMobNoopCallback());
    }

    public void delete(StackMobCallback callback) {
        StackMob.getStackMob().delete(getSchemaName(), id, callback);
    }

}
