package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.DriverStatus;
import lombok.Data;

@Data
public class DriverProfileDto {
    private Long id;

    private Long userId;
    private String fullName;
    private Integer age;
    private String email;
    private String phoneNumber;
    private Boolean isActive;

    private String driverCode;
    private String licenseNumber;
    private String depot;
    private Integer demeritPoints;
    private DriverStatus status;

    private Long activeAssignmentId;
    private Long assignedBusId;
    private String assignedBusCode;
    private String assignedBusDisplayName;
    private String assignedBusRegistrationNumber;

    private Long assignedRouteVariantId;
    private String assignedRouteVariantCode;
    private String assignedRouteNumber;
    private String assignedRouteName;
    private String originName;
    private String destinationName;
    private String directionLabel;

    private Boolean hasActiveQr;
    private String activeQrLabel;
}
