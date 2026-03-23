package com.transitshield.backend.repository;

import com.transitshield.backend.entity.LostItemReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostItemReportRepository extends JpaRepository<LostItemReport, Long> {
    List<LostItemReport> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId);
    List<LostItemReport> findByStatusOrderByCreatedAtDesc(com.transitshield.backend.entity.enums.LostItemStatus status);
}
