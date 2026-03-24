package com.transitshield.backend.controller;

import com.transitshield.backend.dto.ComplaintRequest;
import com.transitshield.backend.entity.Complaint;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Map<String, Object>> createComplaint(
            @AuthenticationPrincipal User user,
            @RequestBody ComplaintRequest request
    ) {
        if (user == null || user.getId() == null) {
            throw new BadRequestException("Authenticated passenger is required");
        }
        if (request == null || request.getTripId() == null || request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BadRequestException("tripId and description are required");
        }

        Complaint complaint = new Complaint();
        complaint.setPassengerUserId(user.getId());
        complaint.setTripId(request.getTripId());
        complaint.setSubject(request.getIncidentType());
        complaint.setDescription(request.getDescription().trim());
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);
        return ResponseEntity.ok(Map.of(
                "message", "Complaint submitted successfully",
                "complaintId", saved.getId()
        ));
    }
}
