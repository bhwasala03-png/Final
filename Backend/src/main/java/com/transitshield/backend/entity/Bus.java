package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.BusStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String busCode;

    private String registrationNumber;

    private String busDisplayName;

    private Integer capacity;

    private String operatorName;

    @Enumerated(EnumType.STRING)
    private BusStatus status;
}
