package com.transitshield.backend.repository;

import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserId(Long userId);

    List<DriverProfile> findByStatus(DriverStatus status);

    boolean existsByUserId(Long userId);

    boolean existsByDriverCode(String driverCode);

    boolean existsByLicenseNumber(String licenseNumber);
}
