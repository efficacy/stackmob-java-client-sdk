package com.stackmob.sdk.util;

public class BinaryFieldFormatter {
    private String contentType;
    private String s3FileName;
    private byte[] data;
    public BinaryFieldFormatter(String contentType, String s3FileName, byte[] data) {
        this.contentType = contentType;
        this.s3FileName = s3FileName;
        this.data = data;
    }

    public String getJsonValue() {
        String encodedBytes = Base64.encode(this.data);
        StringBuilder builder = new StringBuilder();
        builder.append("Content-Type: ").append(this.contentType).append("\n");
        builder.append("Content-Disposition: attachment; filename=").append(this.s3FileName).append("\n");
        builder.append("Content-Transfer-Encoding: ").append("base64").append("\n\n");
        builder.append(encodedBytes);
        return builder.toString();
    }
}
