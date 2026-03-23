package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportCreateRequest;
import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.LostItemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
@RequiredArgsConstructor
public class LostItemReportController {

    private final LostItemReportService lostItemReportService;

    @PostMapping
    public ResponseEntity<LostItemReportDto> create(
            @AuthenticationPrincipal User user,
            @RequestBody LostItemReportCreateRequest request
    ) {
        return ResponseEntity.ok(lostItemReportService.create(user, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LostItemReportDto>> getMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lostItemReportService.getMine(user));
    }
}
