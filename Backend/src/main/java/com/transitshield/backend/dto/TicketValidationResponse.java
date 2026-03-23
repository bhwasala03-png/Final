package com.transitshield.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketValidationResponse {
    private Boolean valid;
    private String status;
    private String message;

    private Long tripId;
    private String tripRef;

    private Long passengerProfileId;
    private String passengerPublicUserId;
    private String passengerName;

    private Long busAssignmentId;
    private Long busId;
    private String busCode;
    private String busDisplayName;

    private Long driverProfileId;
    private String driverName;
    private String driverCode;

    private String routeNumber;
    private String routeName;
    private String originName;
    private String destinationName;

    private Long boardingStopId;
    private String boardingStopName;
    private Long destinationStopId;
    private String destinationStopName;

    private Double totalFareLkr;
    private String paymentStatus;
    private String tripStatus;

    private LocalDateTime issuedAt;
    private LocalDateTime validatedAt;
}
