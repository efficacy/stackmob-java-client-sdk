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
package com.stackmob.sdk.testobjects;

import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;

public class Book extends StackMobModel {

    private String title;
    private String publisher;
    private Author author;


    public Book(StackMob stackmob) {
        super(Book.class);
    }
    public Book(String title, String publisher, Author author, StackMob stackmob) {
        this(stackmob);
        setTitle(title);
        setPublisher(publisher);
        setAuthor(author);
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
