package com.stackmob.sdk.testobjects;

import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.api.StackMobUser;

public class BlogPost extends StackMobModel {

    public BlogPost(StackMob stackmob) {
        super(stackmob, BlogPost.class);
    }
    public String title;
    public String post;
    public StackMobUser owner;
}
