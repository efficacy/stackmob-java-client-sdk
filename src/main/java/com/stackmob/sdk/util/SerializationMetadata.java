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

package com.stackmob.sdk.util;

import com.stackmob.sdk.model.StackMobModel;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores some information about classes in an easily queriable form
 */
public enum SerializationMetadata {
    PRIMITIVE,
    OBJECT,
    MODEL,
    PRIMITIVE_ARRAY,
    OBJECT_ARRAY,
    MODEL_ARRAY;

    public static SerializationMetadata getSerializationMetadata(Class<?> actualClass, String fieldName) {
        ensureMetadata(actualClass);
        return metadataForClasses.get(actualClass).get(fieldName);
    }

    private static Map<Class<?>,Map<String,SerializationMetadata>> metadataForClasses = new HashMap<Class<?>, Map<String, SerializationMetadata>>();

    public static void ensureMetadata(Class<?> actualClass) {
        if(!metadataForClasses.containsKey(actualClass)) {
            metadataForClasses.put(actualClass,new HashMap<String, SerializationMetadata>());
            //Sort the fields into groupings we care about for serialization
            for(Field field : actualClass.getDeclaredFields()) {
                metadataForClasses.get(actualClass).put(field.getName(), determineMetadata(field));
            }
        }
    }

    private static SerializationMetadata determineMetadata(Field field) {
        if(isArray(field.getType())) {
            Class<?> componentClass = getComponentClass(field);
            if(isPrimitive(componentClass)) {
                return PRIMITIVE_ARRAY;
            } else if(isModel(componentClass)) {
                return MODEL_ARRAY;
            } else {
                return OBJECT_ARRAY;
            }
        } else if(isPrimitive(field.getType())) {
            return PRIMITIVE;
        } else if(isModel(field.getType())) {
            return MODEL;
        } else {
            return OBJECT;
        }
    }
    
    private static boolean isArray(Class<?> aClass) {
        return aClass.isArray() || Collection.class.isAssignableFrom(aClass);
    }

    //Given X[] or Collection<X> finds X. If X is
    //parametrized further we ignore it.
    private static Class<?> getComponentClass(Field field) {
        if(field.getType().isArray()) {
            return field.getType().getComponentType();
        }
        Type type = field.getGenericType();
        if(type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type componentType = pType.getActualTypeArguments()[0];
            if(componentType instanceof Class<?>) {
                return (Class<?>) componentType;
            }else if(componentType instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) componentType).getRawType();
            }
        }
        return null;
    }
    
    private static boolean isPrimitive(Class<?> aClass) {
        return aClass.isPrimitive() || aClass.equals(String.class);
    }

    private static boolean isModel(Class<?> aClass) {
        return StackMobModel.class.isAssignableFrom(aClass);
    }
}
