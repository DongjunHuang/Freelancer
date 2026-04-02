package com.example.controllers;

import com.example.dashboard.app.DashboardService;
import com.example.dashboard.domain.FetchRecordsProps;
import com.example.dashboard.domain.FetchRecordsReq;
import com.example.dashboard.domain.FetchRecordsResp;
import com.example.dashboard.interfaces.DashboardController;
import com.example.security.JwtUserDetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTests {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private JwtUserDetails jwtUserDetails;

    @InjectMocks
    private DashboardController controller;

    @Test
    void testQueryDatapointsShouldUseUserIdFromAuthentication() throws Exception {
        Long userId = 123L;

        when(jwtUserDetails.getId()).thenReturn(userId);

        FetchRecordsReq req = new FetchRecordsReq();

        FetchRecordsResp serviceResp = new FetchRecordsResp();
        serviceResp.setDatasetName("my-dataset");

        // TODO
        // serviceResp.setDatapoints(Collections.emptyMap());

        ArgumentCaptor<FetchRecordsProps> propsCaptor = ArgumentCaptor.forClass(FetchRecordsProps.class);

        when(dashboardService.queryDatapoints(eq(userId), propsCaptor.capture())).thenReturn(serviceResp);

        // when
        ResponseEntity<FetchRecordsResp> response = controller.queryDatapoints(req.getDatasetName(), req, jwtUserDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(serviceResp, response.getBody());

        // then
        verify(dashboardService, times(1)).queryDatapoints(eq(userId), any(FetchRecordsProps.class));

        FetchRecordsProps capturedProps = propsCaptor.getValue();
        assertNotNull(capturedProps);
    }
}