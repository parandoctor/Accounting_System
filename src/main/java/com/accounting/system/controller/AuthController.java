package com.accounting.system.controller;

import com.accounting.system.model.dto.LoginDTO;
import com.accounting.system.model.dto.RegisterDTO;
import com.accounting.system.model.vo.LoginVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * 认证控制器
 * ============================================================
 *
 * 【角色定位】
 * Controller层只做三件事：
 * 1. 接收HTTP请求，通过 @Valid 触发参数校验
 * 2. 调用Service层方法
 * 3. 将结果包装为 ResultVO 统一响应
 *
 * 【为什么Controller这么薄？】
 * 所有业务逻辑在Service层，Controller只是透明代理。
 * 好处：Service可被多个入口复用（REST、定时任务、单元测试）
 *
 * 【/api/auth 路径下的接口无需认证】
 * SecurityConfig中配置了 .requestMatchers("/api/auth/**").permitAll()
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * @Valid 自动校验 RegisterDTO 的字段约束（@NotBlank等）
     */
    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return ResultVO.success();
    }

    /**
     * 用户登录
     * 返回 LoginVO（含JWT token和用户信息）
     */
    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = authService.login(dto);
        return ResultVO.success(loginVO);
    }
}
