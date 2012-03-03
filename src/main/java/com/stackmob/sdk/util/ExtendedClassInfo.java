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

import com.stackmob.sdk.api.StackMobModel;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores some information about classes in an easily queriable form
 */
public class ExtendedClassInfo {


    public enum FieldGroup {
        PRIMITIVE,
        OBJECT,
        MODEL,
        OBJECT_ARRAY,
        MODEL_ARRAY
    }
    
    public static FieldGroup getFieldGroup(Class<?> actualClass, String fieldName) {
        ensureFieldGroups(actualClass);
        return fieldGroupsForClasses.get(actualClass).get(fieldName);
    }

    private static Map<Class<?>,Map<String,FieldGroup>> fieldGroupsForClasses = new HashMap<Class<?>, Map<String, FieldGroup>>();
    
    public static void ensureFieldGroups(Class<?> actualClass) {
        if(!fieldGroupsForClasses.containsKey(actualClass)) {
            //Sort the fields into groupings we care about for serialization
            for(Field field : actualClass.getDeclaredFields()) {
                fieldGroupsForClasses.get(actualClass).put(field.getName(), determineFieldGroup(field));
            }
        }
    }

    private static FieldGroup determineFieldGroup(Field field) {
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
}
