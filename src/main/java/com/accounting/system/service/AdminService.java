package com.accounting.system.service;

import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.UserVO;

import java.util.Map;

/**
 * ============================================================
 * 管理员服务接口
 * ============================================================
 *
 * 【权限控制】
 * 仅 ADMIN 角色可访问，由 SecurityConfig 在URL层控制
 *
 * 【管理功能】
 * - listUsers：用户列表（支持关键词模糊搜索）
 * - toggleUserStatus：启用/禁用用户（不能禁用管理员）
 * - resetUserPassword：重置密码为默认值 123456
 * - getSystemStatistics：系统概览统计（用户数、账单数）
 */
public interface AdminService {
    PageVO<UserVO> listUsers(int page, int size, String keyword);
    void toggleUserStatus(Long currentAdminId, Long targetUserId);
    void resetUserPassword(Long userId);
    Map<String, Long> getSystemStatistics();
}
