package com.stackmob.sdk.testobjects;

import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.api.StackMobUser;

public class Book extends StackMobModel {

    public Book(StackMob stackmob) {
        super(stackmob, Book.class);
    }

    private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    private String publisher;
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    private StackMobUser owner;
    public StackMobUser getOwner() {
         return owner;
    }
    public void setOwner(StackMobUser owner) {
        this.owner = owner;
    }
}
