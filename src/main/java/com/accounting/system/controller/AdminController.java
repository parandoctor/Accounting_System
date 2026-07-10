package com.accounting.system.controller;

import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResultVO<PageVO<UserVO>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String keyword) {
        PageVO<UserVO> pageVO = adminService.listUsers(page, size, keyword);
        return ResultVO.success(pageVO);
    }

    @PutMapping("/users/{id}/status")
    public ResultVO<Void> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResultVO.success();
    }

    @PutMapping("/users/{id}/reset-password")
    public ResultVO<Void> resetUserPassword(@PathVariable Long id) {
        adminService.resetUserPassword(id);
        return ResultVO.success();
    }

    @GetMapping("/statistics")
    public ResultVO<Map<String, Long>> getSystemStatistics() {
        Map<String, Long> stats = adminService.getSystemStatistics();
        return ResultVO.success(stats);
    }
}
