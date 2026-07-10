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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResultVO<UserVO> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserVO vo = userService.getProfile(principal.getUserId());
        return ResultVO.success(vo);
    }

    @PutMapping("/profile")
    public ResultVO<UserVO> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody UserUpdateDTO dto) {
        UserVO vo = userService.updateProfile(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(principal.getUserId(), dto);
        return ResultVO.success();
    }
}
