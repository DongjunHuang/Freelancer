package com.example.repos;

public enum MetadataStatus {
    READY("Ready"),
    IMPORTING("Importing"),
    COMPLETED("Completed"),
    FAILED("Failed");


    private final String desc;

    MetadataStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
