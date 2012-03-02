package com.stackmob.sdk.api;

/**
 * Created by IntelliJ IDEA.
 * User: drapp
 * Date: 3/1/12
 * Time: 6:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackMobUser extends StackMobModel {
    public StackMobUser(StackMob stackmob) {
        super(stackmob, StackMobUser.class);
    }

    @Override
    protected String getSchemaName() {
        return "user";
    }

    @Override
    protected String getIDFieldName() {
        return "username";
    }
    
    private String password; //TODO shouldn't be here
    public String getPassword() {
        return password;
    }
    private String email;
    public String getEmail() {
        return email;
    }
}
