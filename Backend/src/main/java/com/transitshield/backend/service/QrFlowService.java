package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.dto.QrScanRequest;
import com.transitshield.backend.dto.QrScanResponse;
import com.transitshield.backend.dto.RouteVariantStopDto;
import com.transitshield.backend.entity.*;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QrFlowService {

    private final BusQrCodeRepository qrCodeRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final PassengerTripRepository passengerTripRepository;
    private final BusRepository busRepository;

    /**
     * Admin generates a QR code for a specific bus.
     * Deactivates any existing active QR for that bus first (one-active-per-bus).
     */
    @Transactional
    public BusQrCodeDto generateQrForBus(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + busId));

        // Deactivate all existing active QRs for this bus
        List<BusQrCode> existingActive = qrCodeRepository.findAllByBusIdAndIsActiveTrue(busId);
        for (BusQrCode existing : existingActive) {
            existing.setIsActive(false);
            qrCodeRepository.save(existing);
        }

        // Create new QR code
        BusQrCode newQr = new BusQrCode();
        newQr.setBus(bus);
        newQr.setQrToken(UUID.randomUUID().toString());
        newQr.setQrLabel("QR-" + bus.getBusCode());
        newQr.setIsActive(true);
        newQr = qrCodeRepository.save(newQr);

        return mapToDto(newQr);
    }

    /**
     * Get the current active QR for a specific bus.
     */
    public BusQrCodeDto getActiveQrForBus(Long busId) {
        BusQrCode qr = qrCodeRepository.findByBusIdAndIsActiveTrue(busId)
                .orElse(null);
        return qr != null ? mapToDto(qr) : null;
    }

    public QrScanResponse scanQr(QrScanRequest request) {
        // 1. Find active QrCode
        BusQrCode qrCode = qrCodeRepository.findByQrTokenAndIsActiveTrue(request.getQrToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive QR code"));

        // 2 & 3. Find bus and active assignment
        List<BusAssignment> assignments = busAssignmentRepository.findByBusIdAndAssignmentStatus(qrCode.getBus().getId(), AssignmentStatus.ACTIVE);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("No active assignment found for this bus");
        }
        BusAssignment activeAssignment = assignments.get(0);

        // 4 & 5. Load RouteVariantStops
        List<RouteVariantStop> stops = routeVariantStopRepository.findByRouteVariantIdOrderByStopOrderAsc(activeAssignment.getRouteVariant().getId());

        // 6. Check if passenger has active trip
        Optional<PassengerTrip> activeTrip = passengerTripRepository.findByPassengerProfileIdAndTripStatus(request.getPassengerId(), TripStatus.ACTIVE);
        if (activeTrip.isPresent() && activeTrip.get().getBusAssignment().getId().equals(activeAssignment.getId())) {
            QrScanResponse response = new QrScanResponse();
            response.setMessage("Active trip already exists for this assignment");
            response.setBusAssignmentId(activeAssignment.getId());
            return response;
        }

        // 7. Calculate Nearest Stop
        Long nearestStopId = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            nearestStopId = calculateNearestStop(request.getLatitude(), request.getLongitude(), stops);
        }

        QrScanResponse response = new QrScanResponse();
        response.setMessage("Ready to start trip");
        response.setBusId(qrCode.getBus().getId());
        response.setBusDisplayName(qrCode.getBus().getBusDisplayName());
        response.setDriverProfileId(activeAssignment.getDriverProfile().getId());
        response.setRouteVariantId(activeAssignment.getRouteVariant().getId());
        response.setBusAssignmentId(activeAssignment.getId());
        response.setNearestBoardingStopId(nearestStopId);
        
        response.setOrderedStops(stops.stream().map(this::mapStopToDto).collect(Collectors.toList()));
        return response;
    }

    private RouteVariantStopDto mapStopToDto(RouteVariantStop entity) {
        RouteVariantStopDto dto = new RouteVariantStopDto();
        dto.setId(entity.getId());
        dto.setRouteVariantId(entity.getRouteVariant().getId());
        dto.setStopId(entity.getStop().getId());
        dto.setStopOrder(entity.getStopOrder());
        dto.setDistanceFromStartKm(entity.getDistanceFromStartKm());
        dto.setIsMajorStop(entity.getIsMajorStop());
        return dto;
    }

    private Long calculateNearestStop(Double passengerLat, Double passengerLon, List<RouteVariantStop> stops) {
        double minDistance = Double.MAX_VALUE;
        Long nearestStopId = null;
        for (RouteVariantStop rvs : stops) {
            Stop stop = rvs.getStop();
            if (stop.getLatitude() != null && stop.getLongitude() != null) {
                double distance = haversine(passengerLat, passengerLon, stop.getLatitude(), stop.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStopId = stop.getId();
                }
            }
        }
        return nearestStopId;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private BusQrCodeDto mapToDto(BusQrCode qr) {
        BusQrCodeDto dto = new BusQrCodeDto();
        dto.setId(qr.getId());
        dto.setBusId(qr.getBus().getId());
        dto.setQrToken(qr.getQrToken());
        dto.setQrLabel(qr.getQrLabel());
        dto.setIsActive(qr.getIsActive());
        return dto;
    }
}
