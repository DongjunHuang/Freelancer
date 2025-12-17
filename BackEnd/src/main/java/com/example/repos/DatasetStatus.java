package com.example.repos;

/**
 * The status of the dataset.
 */
public enum DatasetStatus {
    // The dataset is free for receiving any operations
    ACTIVE("ACTIVE"),

    // User is uploading the datasets
    UPLOADING("UPLOADING"),

    // User is failed of uploading/deleting
    FAILED("FAILED"),

    // User is deleting the datasets
    DELETING("DELETING");

    private final String desc;

    DatasetStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}