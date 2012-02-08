package com.stackmob.sdk.api;

import com.stackmob.sdk.util.BinaryFieldFormatter;

/**
 * Created by IntelliJ IDEA.
 * User: drapp
 * Date: 2/8/12
 * Time: 10:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class StackMobFile {
    
    private String value;
    public StackMobFile(String contentType, String fileName, byte[] bytes) {
        BinaryFieldFormatter formatter = new BinaryFieldFormatter(contentType, fileName, bytes);
        value = formatter.getJsonValue();
    }
    
    @Override
    public String toString() {
        return value;
    }
}
