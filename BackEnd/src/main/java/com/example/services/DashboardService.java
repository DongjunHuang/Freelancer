package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.repos.DatasetMetadata;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return null;
    }
}
