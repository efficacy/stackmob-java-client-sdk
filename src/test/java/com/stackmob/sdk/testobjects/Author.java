package com.stackmob.sdk.testobjects;

import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;

public class Author extends StackMobModel {

    private String name;

    public Author(StackMob stackmob) {
        super(stackmob, Author.class);
    }

    public Author(String name, StackMob stackmob) {
        this(stackmob);
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
