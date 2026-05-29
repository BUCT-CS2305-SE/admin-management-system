package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buct.backend.entity.Artifact;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.ArtifactMapper;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ArtifactMapper artifactMapper;
    private final PlatformUserMapper platformUserMapper;
    private final UserContentMapper userContentMapper;

    public DashboardServiceImpl(ArtifactMapper artifactMapper,
                               PlatformUserMapper platformUserMapper,
                               UserContentMapper userContentMapper) {
        this.artifactMapper = artifactMapper;
        this.platformUserMapper = platformUserMapper;
        this.userContentMapper = userContentMapper;
    }

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        Long artifactCount = artifactMapper.selectCount(null);
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

        LambdaQueryWrapper<Artifact> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Artifact::getAuditStatus, 1);
        Long publishedCount = artifactMapper.selectCount(publishedWrapper);

        LambdaQueryWrapper<Artifact> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Artifact::getAuditStatus, 0);
        Long pendingCount = artifactMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<Artifact> offlineWrapper = new LambdaQueryWrapper<>();
        offlineWrapper.eq(Artifact::getAuditStatus, 2);
        Long offlineCount = artifactMapper.selectCount(offlineWrapper);

        stat.put("total", artifactMapper.selectCount(null));
        stat.put("published", publishedCount);
        stat.put("pending", pendingCount);
        stat.put("offline", offlineCount);

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
}