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
package com.stackmob.sdk.callback;

import com.stackmob.sdk.model.StackMobModel;
import com.stackmob.sdk.exception.StackMobException;

import java.util.List;

public abstract class StackMobQueryCallback<T extends StackMobModel> {
    public abstract void success(List<T> result);
    abstract public void failure(StackMobException e);
}
