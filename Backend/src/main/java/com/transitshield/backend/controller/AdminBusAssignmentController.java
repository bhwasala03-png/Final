package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusAssignmentDto;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.DriverStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.RouteVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class AdminBusAssignmentController {

    private final BusAssignmentRepository busAssignmentRepository;
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RouteVariantRepository routeVariantRepository;

    @GetMapping
    public ResponseEntity<List<BusAssignmentDto>> getAllAssignments() {
        List<BusAssignmentDto> assignments = busAssignmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @PostMapping
    public ResponseEntity<BusAssignmentDto> createAssignment(@RequestBody BusAssignmentDto dto) {
        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        DriverProfile driver = driverProfileRepository.findById(dto.getDriverProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        RouteVariant routeVariant = routeVariantRepository.findById(dto.getRouteVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Route variant not found"));

        completeActiveAssignmentsForBus(bus.getId());
        completeActiveAssignmentsForDriver(driver.getId());

        BusAssignment assignment = new BusAssignment();
        assignment.setBus(bus);
        assignment.setDriverProfile(driver);
        assignment.setRouteVariant(routeVariant);
        assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);
        assignment.setStartedAt(LocalDateTime.now());
        assignment.setEndedAt(null);

        driver.setStatus(DriverStatus.ON_DUTY);
        driverProfileRepository.save(driver);

        BusAssignment saved = busAssignmentRepository.save(assignment);
        return ResponseEntity.ok(mapToDto(saved));
    }

    private void completeActiveAssignmentsForBus(Long busId) {
        List<BusAssignment> activeAssignments = busAssignmentRepository
                .findByBusIdAndAssignmentStatus(busId, AssignmentStatus.ACTIVE);

        activeAssignments.forEach(this::completeAssignment);
    }

    private void completeActiveAssignmentsForDriver(Long driverProfileId) {
        List<BusAssignment> activeAssignments = busAssignmentRepository
                .findByDriverProfileIdAndAssignmentStatus(driverProfileId, AssignmentStatus.ACTIVE);

        activeAssignments.forEach(this::completeAssignment);
    }

    private void completeAssignment(BusAssignment assignment) {
        assignment.setAssignmentStatus(AssignmentStatus.COMPLETED);
        assignment.setEndedAt(LocalDateTime.now());

        DriverProfile previousDriver = assignment.getDriverProfile();
        if (previousDriver != null) {
            previousDriver.setStatus(DriverStatus.AVAILABLE);
            driverProfileRepository.save(previousDriver);
        }

        busAssignmentRepository.save(assignment);
    }

    private BusAssignmentDto mapToDto(BusAssignment entity) {
        BusAssignmentDto dto = new BusAssignmentDto();
        dto.setId(entity.getId());
        dto.setAssignmentStatus(entity.getAssignmentStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());

        Bus bus = entity.getBus();
        if (bus != null) {
            dto.setBusId(bus.getId());
            dto.setBusCode(bus.getBusCode());
            dto.setBusDisplayName(bus.getBusDisplayName());
            dto.setRegistrationNumber(bus.getRegistrationNumber());
        }

        DriverProfile driverProfile = entity.getDriverProfile();
        if (driverProfile != null) {
            dto.setDriverProfileId(driverProfile.getId());
            dto.setDriverCode(driverProfile.getDriverCode());

            User driverUser = driverProfile.getUser();
            if (driverUser != null) {
                dto.setDriverUserId(driverUser.getId());
                dto.setDriverName(driverUser.getFullName());
                dto.setDriverEmail(driverUser.getEmail());
                dto.setDriverPhoneNumber(driverUser.getPhoneNumber());
            }
        }

        RouteVariant routeVariant = entity.getRouteVariant();
        if (routeVariant != null) {
            dto.setRouteVariantId(routeVariant.getId());
            dto.setRouteVariantCode(routeVariant.getVariantCode());
            dto.setOriginName(routeVariant.getOriginName());
            dto.setDestinationName(routeVariant.getDestinationName());
            dto.setDirectionLabel(routeVariant.getDirectionLabel());

            if (routeVariant.getRoute() != null) {
                dto.setRouteNumber(routeVariant.getRoute().getRouteNumber());
                dto.setRouteName(routeVariant.getRoute().getDisplayName());
            }
        }

        return dto;
    }
}
