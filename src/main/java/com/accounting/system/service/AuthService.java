package com.accounting.system.service;

import com.accounting.system.model.dto.LoginDTO;
import com.accounting.system.model.dto.RegisterDTO;
import com.accounting.system.model.vo.LoginVO;

public interface AuthService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
}
