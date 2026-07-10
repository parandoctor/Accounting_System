package com.accounting.system.controller;

import com.accounting.system.model.dto.LoginDTO;
import com.accounting.system.model.dto.RegisterDTO;
import com.accounting.system.model.vo.LoginVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return ResultVO.success();
    }

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = authService.login(dto);
        return ResultVO.success(loginVO);
    }
}
