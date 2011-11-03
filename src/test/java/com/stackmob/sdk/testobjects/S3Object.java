/**
 * Copyright 2011 StackMob
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

package com.stackmob.sdk.testobjects;

import java.util.List;
import com.stackmob.sdk.util.BinaryFieldFormatter;

public class S3Object extends StackMobObject {
    public String s3Object_id;
    public Long createddate;
    public Long lastmoddate;
    public String blob;

    //for Gson
    public S3Object(String s3ObjectId, long createddate, long lastmoddate, String s3FileName) {
        this.s3Object_id = s3ObjectId;
        this.createddate = createddate;
        this.lastmoddate = lastmoddate;
        this.blob = s3FileName;
    }

    //for user
    public S3Object(String contentType, String s3FileName, byte[] bytes) {
        BinaryFieldFormatter formatter = new BinaryFieldFormatter(contentType, s3FileName, bytes);
        this.blob = formatter.getJsonValue();
    }

    @Override public String getIdField() { return s3Object_id; }
    @Override public String getIdFieldName() { return "s3object_id"; }
    @Override public String getName() { return "s3object"; }

}
