package com.example.guards;

import java.util.EnumSet;
import java.util.Map;

import com.example.exception.DatasetStatusException;
import com.example.exception.ErrorCode;
import com.example.repos.DatasetStatus;

public final class DatasetRules {
    // The allows actions->status
    //
    // When user sends action to the backend, which operation is allowed when the
    // dataset is in corresponding dataset action
    public static final Map<DatasetAction, EnumSet<DatasetStatus>> ALLOWED = Map.of(
            DatasetAction.UPLOAD, EnumSet.of(DatasetStatus.ACTIVE),
            DatasetAction.QUERY, EnumSet.of(DatasetStatus.ACTIVE, DatasetStatus.UPLOADING, DatasetStatus.FAILED),
            DatasetAction.DELETE, EnumSet.of(DatasetStatus.ACTIVE, DatasetStatus.FAILED));

    public static void assertAllowed(DatasetAction action, DatasetStatus status) {
        var allowed = ALLOWED.getOrDefault(action, EnumSet.noneOf(DatasetStatus.class));
        if (!allowed.contains(status)) {
            throw new DatasetStatusException(ErrorCode.DATASET_NOT_AVAILABLE);
        }
    }
}