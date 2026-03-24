package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusAssignmentDto;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.PassengerTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketFlowController {

    private final PassengerTicketService passengerTicketService;

    /**
     * Shared authenticated endpoint for listing active bus assignments.
     * Used by the passenger app to choose a live bus/route before starting a trip.
     */
    @GetMapping("/api/tickets/assignments/active")
    public ResponseEntity<List<BusAssignmentDto>> getActiveAssignments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(passengerTicketService.getActiveAssignments());
    }

}
