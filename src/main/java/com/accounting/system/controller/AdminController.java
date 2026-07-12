package com.accounting.system.controller;

import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================
 * 管理员控制器
 * ============================================================
 *
 * 【权限保护】
 * /api/admin/** 路径在 SecurityConfig 中配置了
 * .requestMatchers("/api/admin/**").hasRole("ADMIN")
 * 非管理员用户请求会收到 403 Forbidden
 *
 * 【注意】
 * Admin接口不需要 @AuthenticationPrincipal 获取当前用户,
 * 因为管理员操作的是其他用户的数据，不是自己的
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 用户列表 —— 支持按用户名/昵称搜索
     */
    @GetMapping("/users")
    public ResultVO<PageVO<UserVO>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String keyword) {
        PageVO<UserVO> pageVO = adminService.listUsers(page, size, keyword);
        return ResultVO.success(pageVO);
    }

    /**
     * 切换用户启用/禁用状态
     */
    @PutMapping("/users/{id}/status")
public ResultVO<Void> toggleUserStatus(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
    adminService.toggleUserStatus(principal.getUserId(), id);
    return ResultVO.success();
}

    /**
     * 重置用户密码（设为默认密码 123456）
     */
    @PutMapping("/users/{id}/reset-password")
    public ResultVO<Void> resetUserPassword(@PathVariable Long id) {
        adminService.resetUserPassword(id);
        return ResultVO.success();
    }

    /**
     * 系统概览统计 —— 仪表盘数据
     */
    @GetMapping("/statistics")
    public ResultVO<Map<String, Long>> getSystemStatistics() {
        Map<String, Long> stats = adminService.getSystemStatistics();
        return ResultVO.success(stats);
    }
}
