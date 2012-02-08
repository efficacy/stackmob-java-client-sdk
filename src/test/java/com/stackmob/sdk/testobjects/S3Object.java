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

import com.stackmob.sdk.api.StackMobFile;

public class S3Object extends StackMobObject {
    public String user_id;
    public String username;
    public Long createddate;
    public Long lastmoddate;
    public String photo;

    //for Gson
    public S3Object(String user_id, String username, long createddate, long lastmoddate, String s3FileName) {
        this.user_id = user_id;
        this.createddate = createddate;
        this.lastmoddate = lastmoddate;
        this.photo = s3FileName;
    }

    //for user
    public S3Object(String contentType, String s3FileName, byte[] bytes) {
        this.photo = new StackMobFile(contentType, s3FileName, bytes).toString();
    }

    @Override public String getIdField() { return user_id; }
    @Override public String getIdFieldName() { return "user_id"; }
    @Override public String getName() { return "user"; }

}
