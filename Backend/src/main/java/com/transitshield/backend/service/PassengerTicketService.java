package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusAssignmentDto;
import com.transitshield.backend.dto.RouteVariantStopDto;
import com.transitshield.backend.dto.TicketValidationRequest;
import com.transitshield.backend.dto.TicketValidationResponse;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusQrCode;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.PassengerProfile;
import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.RouteVariantStop;
import com.transitshield.backend.entity.Stop;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusQrCodeRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.PassengerProfileRepository;
import com.transitshield.backend.repository.PassengerTripRepository;
import com.transitshield.backend.repository.RouteVariantStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerTicketService {

    private static final String TRIP_QR_PREFIX = "TS_TRIP:";

    private final BusAssignmentRepository busAssignmentRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final PassengerTripRepository passengerTripRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final BusQrCodeRepository busQrCodeRepository;

    public List<BusAssignmentDto> getActiveAssignments() {
        return busAssignmentRepository.findByAssignmentStatusOrderByStartedAtDesc(AssignmentStatus.ACTIVE)
                .stream()
                .map(this::mapAssignmentToDto)
                .collect(Collectors.toList());
    }

    public PassengerProfile getPassengerProfile(User user) {
        return passengerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger profile not found for user ID: " + user.getId()));
    }

    public PassengerTrip getActiveTrip(User user) {
        PassengerProfile profile = getPassengerProfile(user);
        return passengerTripRepository.findByPassengerProfileIdAndTripStatus(profile.getId(), TripStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active trip found"));
    }

    public List<PassengerTrip> getTripHistory(User user) {
        PassengerProfile profile = getPassengerProfile(user);
        return passengerTripRepository.findByPassengerProfileId(profile.getId()).stream()
                .filter(trip -> trip.getTripStatus() == TripStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    public String buildTicketPayload(PassengerTrip trip) {
        if (trip == null || trip.getTripRef() == null || trip.getTripRef().isBlank()) {
            throw new BadRequestException("Trip reference is not available for ticket generation");
        }
        return TRIP_QR_PREFIX + trip.getTripRef();
    }

    @Transactional
    public void validateTicketByTripId(User driverUser, TicketValidationRequest request) {
        if (request == null || request.getPassengerTripId() == null) {
            throw new BadRequestException("Passenger trip ID is required");
        }

        DriverProfile driverProfile = driverProfileRepository.findByUserId(driverUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + driverUser.getId()));

        BusAssignment activeAssignment = busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(driverProfile.getId(), AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Driver has no active bus assignment"));

        PassengerTrip trip = passengerTripRepository.findById(request.getPassengerTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (trip.getTripStatus() != TripStatus.ACTIVE) {
            throw new BadRequestException("Only active trips can be verified");
        }

        if (trip.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Trip payment is not settled");
        }

        if (trip.getBusAssignment() == null || !trip.getBusAssignment().getId().equals(activeAssignment.getId())) {
            throw new BadRequestException("Trip does not belong to driver's assigned bus");
        }

        LocalDateTime validatedAt = LocalDateTime.now();
        setDriverValidatedAt(trip, validatedAt);
        trip.setIsVerified(true);
        passengerTripRepository.save(trip);
    }

    private BusAssignmentDto mapAssignmentToDto(BusAssignment entity) {
        BusAssignmentDto dto = new BusAssignmentDto();
        dto.setId(entity.getId());
        dto.setAssignmentStatus(entity.getAssignmentStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());

        if (entity.getBus() != null) {
            dto.setBusId(entity.getBus().getId());
            dto.setBusCode(entity.getBus().getBusCode());
            dto.setBusDisplayName(entity.getBus().getBusDisplayName());
            dto.setRegistrationNumber(entity.getBus().getRegistrationNumber());

            BusQrCode activeQr = busQrCodeRepository.findByBusIdAndIsActiveTrue(entity.getBus().getId()).orElse(null);
            dto.setHasActiveQr(activeQr != null);
            if (activeQr != null) {
                dto.setActiveQrLabel(activeQr.getQrLabel());
            }
        }

        if (entity.getDriverProfile() != null) {
            dto.setDriverProfileId(entity.getDriverProfile().getId());
            dto.setDriverCode(entity.getDriverProfile().getDriverCode());

            User driverUser = entity.getDriverProfile().getUser();
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

            List<RouteVariantStopDto> orderedStops = routeVariantStopRepository
                    .findByRouteVariantIdOrderByStopOrderAsc(routeVariant.getId())
                    .stream()
                    .map(this::mapRouteVariantStop)
                    .collect(Collectors.toList());

            setOrderedStopsIfSupported(dto, orderedStops);
        } else {
            dto.setHasActiveQr(Boolean.FALSE.equals(dto.getHasActiveQr()) ? dto.getHasActiveQr() : false);
        }

        return dto;
    }

    private RouteVariantStopDto mapRouteVariantStop(RouteVariantStop entity) {
        RouteVariantStopDto dto = new RouteVariantStopDto();
        dto.setId(entity.getId());
        dto.setRouteVariantId(entity.getRouteVariant() != null ? entity.getRouteVariant().getId() : null);
        dto.setStopId(entity.getStop() != null ? entity.getStop().getId() : null);
        dto.setStopOrder(entity.getStopOrder());
        dto.setDistanceFromStartKm(entity.getDistanceFromStartKm());
        dto.setIsMajorStop(entity.getIsMajorStop());

        if (entity.getStop() != null) {
            setStringIfSupported(dto, "setStopName", entity.getStop().getStopName());
            setStringIfSupported(dto, "setStopCode", entity.getStop().getStopCode());
        }

        return dto;
    }

    private TicketValidationResponse mapTripToValidationResponse(PassengerTrip trip) {
        TicketValidationResponse response = new TicketValidationResponse();
        response.setTripId(trip.getId());
        response.setTripRef(trip.getTripRef());
        response.setIssuedAt(trip.getCreatedAt());
        response.setValidatedAt(getDriverValidatedAt(trip));
        response.setTotalFareLkr(trip.getTotalFareLkr());
        response.setPaymentStatus(trip.getPaymentStatus() != null ? trip.getPaymentStatus().name() : null);
        response.setTripStatus(trip.getTripStatus() != null ? trip.getTripStatus().name() : null);

        if (trip.getPassengerProfile() != null) {
            response.setPassengerProfileId(trip.getPassengerProfile().getId());
            response.setPassengerPublicUserId(trip.getPassengerProfile().getPublicUserId());

            User passengerUser = trip.getPassengerProfile().getUser();
            if (passengerUser != null) {
                response.setPassengerName(passengerUser.getFullName());
            }
        }

        if (trip.getBusAssignment() != null) {
            response.setBusAssignmentId(trip.getBusAssignment().getId());

            if (trip.getBusAssignment().getBus() != null) {
                response.setBusId(trip.getBusAssignment().getBus().getId());
                response.setBusCode(trip.getBusAssignment().getBus().getBusCode());
                response.setBusDisplayName(trip.getBusAssignment().getBus().getBusDisplayName());
            }

            if (trip.getBusAssignment().getDriverProfile() != null) {
                response.setDriverProfileId(trip.getBusAssignment().getDriverProfile().getId());
                response.setDriverCode(trip.getBusAssignment().getDriverProfile().getDriverCode());

                User driverUser = trip.getBusAssignment().getDriverProfile().getUser();
                if (driverUser != null) {
                    response.setDriverName(driverUser.getFullName());
                }
            }

            if (trip.getBusAssignment().getRouteVariant() != null) {
                RouteVariant routeVariant = trip.getBusAssignment().getRouteVariant();
                response.setOriginName(routeVariant.getOriginName());
                response.setDestinationName(routeVariant.getDestinationName());

                if (routeVariant.getRoute() != null) {
                    response.setRouteNumber(routeVariant.getRoute().getRouteNumber());
                    response.setRouteName(routeVariant.getRoute().getDisplayName());
                }
            }
        }

        Stop boarding = trip.getBoardingStop();
        if (boarding != null) {
            response.setBoardingStopId(boarding.getId());
            response.setBoardingStopName(boarding.getStopName());
        }

        Stop destination = trip.getSelectedDestinationStop();
        if (destination != null) {
            response.setDestinationStopId(destination.getId());
            response.setDestinationStopName(destination.getStopName());
        }

        return response;
    }

    private void setOrderedStopsIfSupported(BusAssignmentDto dto, List<RouteVariantStopDto> orderedStops) {
        try {
            Method method = dto.getClass().getMethod("setOrderedStops", List.class);
            method.invoke(dto, orderedStops);
        } catch (Exception ignored) {
            // DTO may not expose ordered stops yet; keep service backward-compatible.
        }
    }

    private void setStringIfSupported(Object target, String methodName, String value) {
        try {
            Method method = target.getClass().getMethod(methodName, String.class);
            method.invoke(target, value);
        } catch (Exception ignored) {
            // DTO may not expose the field yet; keep service backward-compatible.
        }
    }

    private LocalDateTime getDriverValidatedAt(PassengerTrip trip) {
        try {
            Method method = trip.getClass().getMethod("getDriverValidatedAt");
            Object value = method.invoke(trip);
            return value instanceof LocalDateTime ? (LocalDateTime) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setDriverValidatedAt(PassengerTrip trip, LocalDateTime validatedAt) {
        try {
            Method method = trip.getClass().getMethod("setDriverValidatedAt", LocalDateTime.class);
            method.invoke(trip, validatedAt);
        } catch (Exception ignored) {
            throw new BadRequestException("PassengerTrip must support driverValidatedAt before ticket validation can be used");
        }
    }
}
