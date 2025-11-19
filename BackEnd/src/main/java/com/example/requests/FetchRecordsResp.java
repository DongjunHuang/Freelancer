package com.example.requests;

import java.util.List;

public class FetchRecordsResp {

    private String datasetName;

    // 实际返回的列（可能是你请求的子集，也可能是全部）
    private List<String> columns;

    private List<DataRowDto> rows;

    // getter/setter
}
