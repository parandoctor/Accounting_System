package com.accounting.system.controller;

import com.accounting.system.model.dto.PasswordChangeDTO;
import com.accounting.system.model.dto.UserUpdateDTO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.security.UserPrincipal;
import com.accounting.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * 用户控制器
 * ============================================================
 *
 * 【个人中心】
 * 所有接口通过 @AuthenticationPrincipal 获取当前登录用户,
 * 只能操作自己的信息，无法越权访问其他用户数据
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public ResultVO<UserVO> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserVO vo = userService.getProfile(principal.getUserId());
        return ResultVO.success(vo);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public ResultVO<UserVO> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody UserUpdateDTO dto) {
        UserVO vo = userService.updateProfile(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    /**
     * 修改密码（需验证旧密码）
     */
    @PutMapping("/password")
    public ResultVO<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(principal.getUserId(), dto);
        return ResultVO.success();
    }
}
