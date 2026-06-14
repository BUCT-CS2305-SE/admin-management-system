package com.buct.backend.controller;

import com.buct.backend.common.Result;
import com.buct.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        Map<String, Object> summary = dashboardService.getSummary();
        return Result.success(summary);
    }

    @GetMapping("/artifact-stat")
    public Result<Map<String, Object>> getArtifactStat() {
        Map<String, Object> stat = dashboardService.getArtifactStat();
        return Result.success(stat);
    }

    @GetMapping("/content-stat")
    public Result<Map<String, Object>> getContentStat() {
        Map<String, Object> stat = dashboardService.getContentStat();
        return Result.success(stat);
    }
}