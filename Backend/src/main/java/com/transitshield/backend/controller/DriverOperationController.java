package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.dto.PassengerTripDto;
import com.transitshield.backend.dto.TicketValidationRequest;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.DriverOperationsService;
import com.transitshield.backend.service.LostItemReportService;
import com.transitshield.backend.service.PassengerTicketService;
import com.transitshield.backend.service.PassengerTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverOperationController {

    private final LostItemReportService lostItemReportService;
    private final DriverOperationsService driverOperationsService;
    private final PassengerTripService passengerTripService;
    private final PassengerTicketService passengerTicketService;

    @GetMapping("/lost-items")
    public ResponseEntity<List<LostItemReportDto>> getDriverLostItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lostItemReportService.getForDriverAssignedBus(user));
    }

    @GetMapping("/manifest")
    public ResponseEntity<List<PassengerTripDto>> getDriverManifest(@AuthenticationPrincipal User user) {
        Long assignmentId = driverOperationsService.getActiveAssignmentIdForDriver(user);
        return ResponseEntity.ok(passengerTripService.getActiveTripsByBusAssignment(assignmentId));
    }

    @PostMapping("/tickets/validate")
    public ResponseEntity<Map<String, String>> validatePassengerTicket(
            @AuthenticationPrincipal User user,
            @RequestBody TicketValidationRequest request
    ) {
        passengerTicketService.validateTicketByTripId(user, request);
        return ResponseEntity.ok(Map.of("message", "Ticket verified successfully."));
    }
}
