package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buct.backend.client.KgArtifactClient;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final KgArtifactClient kgClient;
    private final PlatformUserMapper platformUserMapper;
    private final UserContentMapper userContentMapper;

    public DashboardServiceImpl(KgArtifactClient kgClient,
                               PlatformUserMapper platformUserMapper,
                               UserContentMapper userContentMapper) {
        this.kgClient = kgClient;
        this.platformUserMapper = platformUserMapper;
        this.userContentMapper = userContentMapper;
    }

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        long artifactCount = fetchArtifactTotal();
        Long userCount = platformUserMapper.selectCount(null);

        LambdaQueryWrapper<UserContent> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(UserContent::getAuditStatus, 0);
        Long pendingCount = userContentMapper.selectCount(pendingWrapper);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        LambdaQueryWrapper<UserContent> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.between(UserContent::getSubmitTime, todayStart, todayEnd);
        Long todayContentCount = userContentMapper.selectCount(todayWrapper);

        summary.put("artifactCount", artifactCount);
        summary.put("userCount", userCount);
        summary.put("pendingCount", pendingCount);
        summary.put("todayContentCount", todayContentCount);

        return summary;
    }

    @Override
    public Map<String, Object> getArtifactStat() {
        Map<String, Object> stat = new HashMap<>();
        long total = fetchArtifactTotal();
        // KG 没有 audit_status 概念，全部视为 published
        stat.put("total", total);
        stat.put("published", total);
        stat.put("pending", 0L);
        stat.put("offline", 0L);
        return stat;
    }

    @Override
    public Map<String, Object> getContentStat() {
        Map<String, Object> stat = new HashMap<>();

        Long total = userContentMapper.selectCount(null);

        LambdaQueryWrapper<UserContent> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(UserContent::getAuditStatus, 0);
        Long pendingCount = userContentMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<UserContent> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(UserContent::getAuditStatus, 1);
        Long approvedCount = userContentMapper.selectCount(approvedWrapper);

        LambdaQueryWrapper<UserContent> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(UserContent::getAuditStatus, 2);
        Long rejectedCount = userContentMapper.selectCount(rejectedWrapper);

        stat.put("total", total);
        stat.put("pending", pendingCount);
        stat.put("approved", approvedCount);
        stat.put("rejected", rejectedCount);

        return stat;
    }

    private long fetchArtifactTotal() {
        try {
            Map<String, Object> resp = kgClient.listArtifacts(1, 1, null, null);
            return KgArtifactClient.readTotal(resp);
        } catch (Exception e) {
            log.warn("获取文物总数失败，临时返回 0：{}", e.getMessage());
            return 0L;
        }
    }
}
