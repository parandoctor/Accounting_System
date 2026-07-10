package com.accounting.system.service;

import com.accounting.system.model.dto.PasswordChangeDTO;
import com.accounting.system.model.dto.UserUpdateDTO;
import com.accounting.system.model.vo.UserVO;

public interface UserService {
    UserVO getProfile(Long userId);
    UserVO updateProfile(Long userId, UserUpdateDTO dto);
    void changePassword(Long userId, PasswordChangeDTO dto);
}
