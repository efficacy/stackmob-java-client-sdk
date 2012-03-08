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

package com.stackmob.sdk.model;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobIntermediaryCallback;
import com.stackmob.sdk.callback.StackMobNoopCallback;
import com.stackmob.sdk.util.SerializationMetadata;
import static com.stackmob.sdk.util.SerializationMetadata.*;

import java.lang.reflect.Array;
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
     * @return the schema name
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
                    StackMobModel relatedModel = (StackMobModel) field.get(this);
                    // If there's a model with the same id, keep it. Otherwise create a new one
                    if(relatedModel == null || !relatedModel.hasSameID(json)) {
                        relatedModel = (StackMobModel) field.getType().newInstance();
                    }
                    relatedModel.fillFromJSON(json);
                    field.set(this, relatedModel);
                } else if(getMetadata(fieldName) == MODEL_ARRAY) {
                    fillModelArrayFieldFromJson(field, json.getAsJsonArray());
                } else if(getMetadata(fieldName) == OBJECT) {
                    //unpack the object string
                    field.set(this, new Gson().fromJson(json.getAsJsonPrimitive().getAsString(), field.getType()));
                } else {
                    // Let gson do its thing
                    field.set(this, new Gson().fromJson(json, field.getType()));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void fillModelArrayFieldFromJson(Field field, JsonArray array) throws IllegalAccessException, InstantiationException {
        Class<? extends StackMobModel> actualModelClass = (Class<? extends StackMobModel>) SerializationMetadata.getComponentClass(field);
        Collection<StackMobModel> existingModels = getFieldAsCollection(field);
        List<StackMobModel> newModels = updateModelListFromJson(array, existingModels, actualModelClass);
        setFieldFromList(field, newModels, actualModelClass);

    }

    /**
     * Turns a field which is either an Array or Collection of StackMobModels and turns in into a collection
     */
    protected Collection<StackMobModel> getFieldAsCollection(Field field) throws IllegalAccessException {
        if(field.getType().isArray()) {
            // grab the existing collection/array if there is one. We want to reuse any existing objects.
            // Otherwise we might end up clobbering a full object with just an id.
            StackMobModel[] models = (StackMobModel[]) field.get(this);
            return models == null ? null : Arrays.asList(models);
        } else {
            return (Collection<StackMobModel>) field.get(this);
        }
    }

    /**
     * Sets a field which is either an Array or Collection of StackMobModels using a list
     */
    protected void setFieldFromList(Field field, List<? extends StackMobModel> list, Class<? extends StackMobModel> modelClass) throws IllegalAccessException, InstantiationException {
        if(field.getType().isArray()) {
            field.set(this, Array.newInstance(modelClass,list.size()));
            StackMobModel[] newModelArray = (StackMobModel[]) field.get(this);
            for(int i = 0; i < list.size(); i++) {
                newModelArray[i] = list.get(i);
            }
        } else {
            // Given a null Collection, how to we find the right concrete collection to use? There is no good way.
            // So let's at least use the same hack as gson.
            field.set(this,new ConstructorConstructor().getConstructor(TypeToken.get(field.getType())).construct());
            Collection<StackMobModel> models = (Collection<StackMobModel>)field.get(this);
            models.clear();
            models.addAll(list);
        }
    }
    
    protected static List<StackMobModel> updateModelListFromJson(JsonArray array, Collection<? extends StackMobModel> existingModels, Class<? extends StackMobModel> modelClass) throws IllegalAccessException, InstantiationException {
        List<StackMobModel> result = new ArrayList<StackMobModel>();
        for(JsonElement json : array) {
            StackMobModel model = getExistingModel(existingModels, json);
            if(model == null) model = modelClass.newInstance();
            model.fillFromJSON(json);
            result.add(model);
        }
        return result;
    }

    /**
     * Finds a model with the same id as the json
     * @param oldList The data in the object already
     * @param json
     * @return
     */
    protected static StackMobModel getExistingModel(Collection<? extends StackMobModel> oldList, JsonElement json) {
        if(oldList != null) {
            for(StackMobModel model : oldList) {
                if(model.hasSameID(json)) {
                    return model;
                }
            }
        }
        return null;
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

    /**
     * Checks if the current object has the same id as this json
     * @param json
     * @return
     */
    protected boolean hasSameID(JsonElement json) {
        if(getID() == null) return false;
        if(json.isJsonPrimitive()) {
            return getID().equals(json.getAsJsonPrimitive().getAsString());
        }
        JsonElement idFromJson = json.getAsJsonObject().get(getIDFieldName());
        return idFromJson == null ? false : getID().equals(idFromJson.getAsString());
    }
    
    private List<String> getFieldNames(JsonObject json) {
        List<String> list = new ArrayList<String>();
        for(Map.Entry<String,JsonElement> entry : json.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    private JsonElement toJSONElement(int depth) {
        if(depth < 0) {
            return getID() == null ? null : new JsonPrimitive(getID());
        }
        JsonObject json = new Gson().toJsonTree(this).getAsJsonObject();
        for(String fieldName : getFieldNames(json)) {
            ensureValidName(fieldName, "field");
            JsonElement value = json.get(fieldName);
            if(getMetadata(fieldName) == MODEL) {
                json.remove(fieldName);
                try {
                    Field relationField = getField(fieldName);
                    relationField.setAccessible(true);
                    StackMobModel relatedModel = (StackMobModel) relationField.get(this);
                    JsonElement relatedJson = relatedModel.toJSONElement(depth -1);
                    if(relatedJson != null) json.add(fieldName, relatedJson);
                } catch (Exception ignore) { } //Should never happen
            } else if(getMetadata(fieldName) == MODEL_ARRAY) {
                json.remove(fieldName);
                try {
                    Field relationField = getField(fieldName);
                    relationField.setAccessible(true);
                    JsonArray array = new JsonArray();
                    Collection<StackMobModel> relatedModels;
                    if(relationField.getType().isArray()) {
                        relatedModels = Arrays.asList((StackMobModel[])relationField.get(this));
                    } else {
                        relatedModels = (Collection<StackMobModel>) relationField.get(this);
                    }
                    for(StackMobModel relatedModel : relatedModels) {
                        JsonElement relatedJson = relatedModel.toJSONElement(depth -1);
                        if(relatedJson != null) array.add(relatedJson);
                    }
                    json.add(fieldName, array);
                } catch (Exception e) { e.printStackTrace();} //Should never happen
            } else if(getMetadata(fieldName) == OBJECT) {
                //We don't support actual objects in our schemas,
                //so condense these down to strings
                String jsonString = value.toString();
                json.remove(fieldName);
                json.add(fieldName, new JsonPrimitive(jsonString));
            }
        }
        if(id != null) {
            json.addProperty(getIDFieldName(),id);
        }
        return json;
    }
    
    protected String toJSON() {
        return toJSON(0);
    }

    /**
     * Converts the object to JSON turning all Models into their ids
     * @return the json representation of this model
     */
    protected String toJSON(int depth) {
        return toJSONElement(depth).toString();
    }
    
    public void create() {
        create(new StackMobNoopCallback());
    }

    public void create(StackMobCallback callback) {
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
