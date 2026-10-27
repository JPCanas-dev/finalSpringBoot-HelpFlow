package com.helpflow.technician_dispatch_service.controller;

import com.helpflow.technician_dispatch_service.dto.DispatchDto.DispatchRequest;
import com.helpflow.technician_dispatch_service.dto.DispatchDto.DispatchResponse;
import com.helpflow.technician_dispatch_service.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Endpoint POST /technician-dispatch
@RestController
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/technician-dispatch")
    public ResponseEntity<DispatchResponse> dispatch(@RequestBody DispatchRequest request) {
        DispatchResponse response = dispatchService.dispatch(request);
        return ResponseEntity.ok(response);
    }
}
