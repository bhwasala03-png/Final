package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.LostItemStatus;
import lombok.Data;

@Data
public class LostItemStatusUpdateRequest {
    private LostItemStatus status;
    private String adminNotes;
    private String resolutionNotes;
}
