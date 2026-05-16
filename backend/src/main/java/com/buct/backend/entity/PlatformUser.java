package com.buct.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_user")
public class PlatformUser {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String source;
    private Integer status;
    private Integer banComment;
    private Integer banUpload;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}