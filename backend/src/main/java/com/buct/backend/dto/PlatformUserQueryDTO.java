package com.buct.backend.dto;

import lombok.Data;

@Data
public class PlatformUserQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String source;
    private Integer status;
    private Integer banComment;
    private Integer banUpload;
}