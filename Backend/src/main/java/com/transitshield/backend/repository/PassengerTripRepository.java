package com.transitshield.backend.repository;

import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassengerTripRepository extends JpaRepository<PassengerTrip, Long> {

    Optional<PassengerTrip> findByPassengerProfileIdAndTripStatus(Long passengerProfileId, TripStatus tripStatus);

    List<PassengerTrip> findByPassengerProfileId(Long passengerProfileId);

    Optional<PassengerTrip> findByTripRef(String tripRef);

    List<PassengerTrip> findByBusAssignmentId(Long busAssignmentId);

    List<PassengerTrip> findByBusAssignmentIdAndTripStatus(Long busAssignmentId, TripStatus tripStatus);

    long countByBusAssignmentDriverProfileIdAndCreatedAtBetween(
            Long driverProfileId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByBusAssignmentDriverProfileIdAndTripStatusAndCreatedAtBetween(
            Long driverProfileId,
            TripStatus tripStatus,
            LocalDateTime start,
            LocalDateTime end
    );
}
