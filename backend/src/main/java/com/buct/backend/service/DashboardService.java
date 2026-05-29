package com.buct.backend.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getSummary();

    Map<String, Object> getArtifactStat();

    Map<String, Object> getContentStat();
}