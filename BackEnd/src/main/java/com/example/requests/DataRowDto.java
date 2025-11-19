package com.example.requests;

import java.time.LocalDate;
import java.util.Map;

public class DataRowDto {

    private LocalDate recordDate;          // 方便前端画图/排序
    private Map<String, Object> values;    // 每一行的列数据

    // getter/setter
}