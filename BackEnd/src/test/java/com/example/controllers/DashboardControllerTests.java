package com.example.controllers;

import com.example.models.FetchRecordsProps;
import com.example.requests.FetchRecordsReq;
import com.example.requests.FetchRecordsResp;
import com.example.security.JwtUserDetails;
import com.example.services.DashboardService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTests {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUserDetails principal;

    @InjectMocks
    private DashboardController controller;

    @Test
    void testQueryDatapointsShouldUseUserIdFromAuthentication() throws Exception {
        Long userId = 123L;

        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getId()).thenReturn(userId);

        FetchRecordsReq req = new FetchRecordsReq();

        FetchRecordsResp serviceResp = new FetchRecordsResp();
        serviceResp.setDatasetName("my-dataset");

        // TODO
        // serviceResp.setDatapoints(Collections.emptyMap());

        ArgumentCaptor<FetchRecordsProps> propsCaptor = ArgumentCaptor.forClass(FetchRecordsProps.class);

        when(dashboardService.queryDatapoints(eq(userId), propsCaptor.capture())).thenReturn(serviceResp);

        // when
        ResponseEntity<FetchRecordsResp> response = controller.queryDatapoints(authentication, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(serviceResp, response.getBody());

        // then
        verify(dashboardService, times(1)).queryDatapoints(eq(userId), any(FetchRecordsProps.class));

        FetchRecordsProps capturedProps = propsCaptor.getValue();
        assertNotNull(capturedProps);

        verify(authentication).getPrincipal();
        verify(principal).getId();
        verifyNoMoreInteractions(dashboardService, authentication, principal);
    }
}