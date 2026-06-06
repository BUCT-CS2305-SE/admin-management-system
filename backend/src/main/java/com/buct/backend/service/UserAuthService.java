package com.buct.backend.service;

import com.buct.backend.dto.LoginDTO;
import com.buct.backend.dto.UserLoginResponseDTO;
import com.buct.backend.dto.UserRegisterDTO;
import com.buct.backend.entity.PlatformUser;

public interface UserAuthService {

    UserLoginResponseDTO login(LoginDTO loginDTO, String ip);

    UserLoginResponseDTO register(UserRegisterDTO registerDTO, String ip);

    PlatformUser getProfile(Long userId);
}
