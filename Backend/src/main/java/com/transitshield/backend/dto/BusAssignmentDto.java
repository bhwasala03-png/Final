package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.AssignmentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BusAssignmentDto {
    private Long id;

    private Long busId;
    private String busCode;
    private String busDisplayName;
    private String registrationNumber;

    private Long driverProfileId;
    private Long driverUserId;
    private String driverName;
    private String driverCode;
    private String driverEmail;
    private String driverPhoneNumber;

    private Long routeVariantId;
    private String routeVariantCode;
    private String routeNumber;
    private String routeName;
    private String originName;
    private String destinationName;
    private String directionLabel;

    private AssignmentStatus assignmentStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private Boolean hasActiveQr;
    private String activeQrLabel;
    private String activeQrToken;

    private List<RouteVariantStopDto> orderedStops;
}
