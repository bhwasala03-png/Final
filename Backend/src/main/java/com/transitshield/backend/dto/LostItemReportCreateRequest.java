package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class LostItemReportCreateRequest {
    private String itemTitle;
    private String description;
    private String category;
    private String routeInfo;
    private String busInfo;
    private String lostAt;
    private String contactDetails;
}
