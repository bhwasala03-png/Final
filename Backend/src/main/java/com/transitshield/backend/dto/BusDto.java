package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.BusStatus;
import lombok.Data;

@Data
public class BusDto {
    private Long id;
    private String busCode;
    private String registrationNumber;
    private String busDisplayName;
    private Integer capacity;
    private String operatorName;
    private BusStatus status;

    private Long activeAssignmentId;
    private Long assignedDriverProfileId;
    private Long assignedDriverUserId;
    private String assignedDriverName;
    private String assignedDriverCode;

    private Long routeVariantId;
    private String routeNumber;
    private String routeName;
    private String routeVariantCode;
    private String originName;
    private String destinationName;
    private String directionLabel;

    private Boolean hasActiveQr;
    private String activeQrLabel;
}
