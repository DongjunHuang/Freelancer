package com.example.requests;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

// 用户请求获取记录
@Data
public class FetchRecordsReq {

    // 哪个 dataset
    private String datasetName;

    // 时间范围（这里用 recordDate，如果你想用 uploadDate 也可以改）
    private LocalDate fromDate;
    private LocalDate toDate;

    // 想要的列名（如果为空/null，就返回所有列）
    private List<String> columns;

    // getter/setter 省略或用 @Data
}