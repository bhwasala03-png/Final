package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class ComplaintRequest {
    private Long tripId;
    private String description;
    private String incidentType;
}
