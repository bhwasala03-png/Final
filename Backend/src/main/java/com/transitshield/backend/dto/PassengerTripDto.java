package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.BoardingDetectMethod;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PassengerTripDto {
    private Long id;
    private String tripRef;

    private Long passengerProfileId;
    private String passengerPublicUserId;
    private String passengerName;

    private Long busAssignmentId;
    private Long busId;
    private String busCode;
    private String busDisplayName;
    private String registrationNumber;

    private Long driverProfileId;
    private Long driverUserId;
    private String driverName;
    private String driverCode;

    private Long routeVariantId;
    private String routeVariantCode;
    private String routeNumber;
    private String routeName;
    private String originName;
    private String destinationName;
    private String directionLabel;

    private String qrTokenUsed;
    private String ticketQrPayload;
    private Boolean driverValidated;
    private LocalDateTime driverValidatedAt;
    private Boolean isVerified;
    private String destinationStop;

    private Long boardingStopId;
    private String boardingStopName;
    private BoardingDetectMethod boardingDetectMethod;

    private Long selectedDestinationStopId;
    private String selectedDestinationStopName;

    private Long actualExitStopId;
    private String actualExitStopName;

    private Double baseFareLkr;
    private Double extraFareLkr;
    private Double totalFareLkr;

    private PaymentStatus paymentStatus;
    private TripStatus tripStatus;

    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
