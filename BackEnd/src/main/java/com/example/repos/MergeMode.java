package com.example.repos;

public enum MergeMode {
    MERGING("MergingMode");
    

    private final String mode;

    MergeMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
