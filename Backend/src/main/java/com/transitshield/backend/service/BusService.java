package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusDto;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusQrCode;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusQrCodeRepository;
import com.transitshield.backend.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final BusQrCodeRepository busQrCodeRepository;

    public List<BusDto> findAll() {
        return busRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BusDto findById(Long id) {
        return busRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public BusDto create(BusDto dto) {
        Bus bus = mapToEntity(dto);
        bus = busRepository.save(bus);
        return mapToDto(bus);
    }

    public BusDto update(Long id, BusDto dto) {
        return busRepository.findById(id).map(bus -> {
            bus.setBusCode(dto.getBusCode());
            bus.setRegistrationNumber(dto.getRegistrationNumber());
            bus.setBusDisplayName(dto.getBusDisplayName());
            bus.setCapacity(dto.getCapacity());
            bus.setOperatorName(dto.getOperatorName());
            bus.setStatus(dto.getStatus());
            return mapToDto(busRepository.save(bus));
        }).orElse(null);
    }

    public void delete(Long id) {
        busRepository.deleteById(id);
    }

    private BusDto mapToDto(Bus bus) {
        BusDto dto = new BusDto();
        dto.setId(bus.getId());
        dto.setBusCode(bus.getBusCode());
        dto.setRegistrationNumber(bus.getRegistrationNumber());
        dto.setBusDisplayName(bus.getBusDisplayName());
        dto.setCapacity(bus.getCapacity());
        dto.setOperatorName(bus.getOperatorName());
        dto.setStatus(bus.getStatus());

        BusQrCode activeQr = busQrCodeRepository.findByBusIdAndIsActiveTrue(bus.getId()).orElse(null);
        if (activeQr != null) {
            dto.setHasActiveQr(Boolean.TRUE);
            dto.setActiveQrLabel(activeQr.getQrLabel());
        } else {
            dto.setHasActiveQr(Boolean.FALSE);
        }

        BusAssignment activeAssignment = busAssignmentRepository
                .findFirstByBusIdAndAssignmentStatusOrderByStartedAtDesc(bus.getId(), AssignmentStatus.ACTIVE)
                .orElse(null);

        if (activeAssignment != null) {
            dto.setActiveAssignmentId(activeAssignment.getId());

            if (activeAssignment.getDriverProfile() != null) {
                dto.setAssignedDriverProfileId(activeAssignment.getDriverProfile().getId());
                dto.setAssignedDriverCode(activeAssignment.getDriverProfile().getDriverCode());

                User driverUser = activeAssignment.getDriverProfile().getUser();
                if (driverUser != null) {
                    dto.setAssignedDriverUserId(driverUser.getId());
                    dto.setAssignedDriverName(driverUser.getFullName());
                }
            }

            RouteVariant routeVariant = activeAssignment.getRouteVariant();
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
        }

        return dto;
    }

    private Bus mapToEntity(BusDto dto) {
        Bus bus = new Bus();
        bus.setId(dto.getId());
        bus.setBusCode(dto.getBusCode());
        bus.setRegistrationNumber(dto.getRegistrationNumber());
        bus.setBusDisplayName(dto.getBusDisplayName());
        bus.setCapacity(dto.getCapacity());
        bus.setOperatorName(dto.getOperatorName());
        bus.setStatus(dto.getStatus());
        return bus;
    }
}
