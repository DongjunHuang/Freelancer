package com.example.async.importdataset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportResult {
    private long processedRows;
    private long successRows;
    private long failedRows;
}