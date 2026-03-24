package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.LostItemStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "lost_item_reports")
@Data
public class LostItemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporterUser;

    @Column(nullable = false, length = 120)
    private String itemTitle;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 80)
    private String category;

    @Column(length = 120)
    private String routeInfo;

    @Column(length = 120)
    private String busInfo;

    private Long busId;

    private LocalDateTime lostAt;

    @Column(length = 220)
    private String contactDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LostItemStatus status;

    @Column(length = 1200)
    private String adminNotes;

    @Column(length = 1200)
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
