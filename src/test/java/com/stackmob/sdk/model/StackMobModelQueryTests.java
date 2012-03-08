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

import com.stackmob.sdk.StackMobTestCommon;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.model.StackMobModelQuery;
import com.stackmob.sdk.testobjects.Author;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.latchOne;

public class StackMobModelQueryTests extends StackMobTestCommon {

    final MultiThreadAsserter asserter = new MultiThreadAsserter();
    final CountDownLatch latch = latchOne();

    @Test public void testQuery() throws Exception {
        StackMobModelQuery<Author> query = new StackMobModelQuery<Author>(Author.class);
        query.getQuery().isInRange(0,10);
        query.send(new StackMobQueryCallback<Author>() {
            @Override
            public void success(List<Author> result) {
                assertEquals(11, result.size());
                assertNotNull(result.get(0).getName());
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
    }
}