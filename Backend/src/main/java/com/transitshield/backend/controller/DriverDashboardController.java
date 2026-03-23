package com.transitshield.backend.controller;

import com.transitshield.backend.dto.AlertDto;
import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.dto.DriverDashboardDto;
import com.transitshield.backend.dto.DriverScheduleDto;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.DriverOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverDashboardController {

    private final DriverOperationsService driverOperationsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DriverDashboardDto> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(driverOperationsService.getDashboard(user));
    }

    @GetMapping("/schedule")
    public ResponseEntity<DriverScheduleDto> getSchedule(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(driverOperationsService.getSchedule(user));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDto>> getAlerts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(driverOperationsService.getAlerts(user));
    }

    @GetMapping("/assigned-bus/qr")
    public ResponseEntity<BusQrCodeDto> getAssignedBusQr(@AuthenticationPrincipal User user) {
        BusQrCodeDto dto = driverOperationsService.getAssignedBusQr(user);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }
}
