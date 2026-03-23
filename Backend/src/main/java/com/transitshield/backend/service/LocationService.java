package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusLocationDto;
import com.transitshield.backend.dto.LocationUpdateRequest;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusLocation;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusLocationRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private static final double DEFAULT_CITY_SPEED_KMH = 25.0;

    private final BusLocationRepository locationRepository;
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    public void updateLocation(LocationUpdateRequest request) {
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        DriverProfile driver = driverProfileRepository.findById(request.getDriverProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        List<BusAssignment> activeAssignments = busAssignmentRepository.findByBusIdAndAssignmentStatus(
                bus.getId(),
                AssignmentStatus.ACTIVE
        );

        BusLocation location = new BusLocation();
        location.setBus(bus);
        location.setDriverProfile(driver);

        if (!activeAssignments.isEmpty()) {
            location.setRouteVariant(activeAssignments.get(0).getRouteVariant());
        }

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setSpeedKmh(request.getSpeedKmh());
        location.setHeading(request.getHeading());
        location.setOccupancyStatus(request.getOccupancyStatus());
        location.setSourceType(request.getSourceType());
        location.setRecordedAt(LocalDateTime.now());

        locationRepository.save(location);
    }

    public List<BusLocationDto> getLiveLocations() {
        Map<Long, BusLocation> latestByBus = new LinkedHashMap<>();

        locationRepository.findAll().stream()
                .sorted(Comparator.comparing(BusLocation::getRecordedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .forEach(location -> latestByBus.putIfAbsent(location.getBus().getId(), location));

        return latestByBus.values().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BusLocationDto getBusLocation(Long busId) {
        return locationRepository.findTopByBusIdOrderByRecordedAtDesc(busId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No location found for this bus"));
    }

    public List<BusLocationDto> getRouteVariantLocations(Long routeVariantId) {
        Map<Long, BusLocation> latestByBus = new LinkedHashMap<>();

        locationRepository.findByRouteVariantIdOrderByRecordedAtDesc(routeVariantId)
                .forEach(location -> latestByBus.putIfAbsent(location.getBus().getId(), location));

        return latestByBus.values().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BusLocationDto mapToDto(BusLocation entity) {
        BusLocationDto dto = new BusLocationDto();

        dto.setId(entity.getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setSpeedKmh(entity.getSpeedKmh());
        dto.setHeading(entity.getHeading());
        dto.setOccupancyStatus(entity.getOccupancyStatus());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setSourceType(entity.getSourceType());

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

            if (entity.getLatitude() != null
                    && entity.getLongitude() != null
                    && routeVariant.getDestinationName() != null) {
                // No destination coordinates are modeled yet, so ETA is estimated from latest speed
                // using a conservative in-city fallback distance heuristic.
                double estimatedDistanceKm = estimateDistanceToDestination(entity);
                dto.setDistanceToDestinationKm(round(estimatedDistanceKm));
                dto.setEtaMinutes(calculateEtaMinutes(estimatedDistanceKm, entity.getSpeedKmh()));
            }
        }

        return dto;
    }

    private double estimateDistanceToDestination(BusLocation entity) {
        RouteVariant routeVariant = entity.getRouteVariant();
        if (routeVariant == null) {
            return 0.0;
        }

        // Until destination coordinates are modeled, use a simple bounded heuristic:
        // faster buses with a recorded route are assumed to be within a practical demo-ready city range.
        double speed = entity.getSpeedKmh() != null && entity.getSpeedKmh() > 0 ? entity.getSpeedKmh() : DEFAULT_CITY_SPEED_KMH;

        if (speed >= 40) return 3.0;
        if (speed >= 25) return 5.0;
        if (speed >= 10) return 8.0;
        return 10.0;
    }

    private Integer calculateEtaMinutes(double distanceKm, Double speedKmh) {
        double safeSpeed = speedKmh != null && speedKmh > 0 ? speedKmh : DEFAULT_CITY_SPEED_KMH;
        int eta = (int) Math.ceil((distanceKm / safeSpeed) * 60.0);
        return Math.max(1, eta);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
