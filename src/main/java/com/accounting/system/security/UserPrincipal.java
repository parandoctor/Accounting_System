package com.accounting.system.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ============================================================
 * 用户认证主体 —— 贯穿整个请求生命周期的用户身份载体
 * ============================================================
 *
 * 【设计定位】
 * 这是JWT认证模式下，从Token解析出的用户信息在服务端的"代言人"。
 * 它不是数据库实体，而是从Token中提取的核心身份信息的快照。
 *
 * 【与Spring Security的关系】
 * UserPrincipal 被包装在 UsernamePasswordAuthenticationToken 中，
 * 存入 SecurityContextHolder。Controller层通过 @AuthenticationPrincipal
 * 注解可以直接注入当前登录用户：
 *
 *   @GetMapping("/profile")
 *   public ResultVO<UserVO> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
 *       // principal.getUserId() 即为当前登录用户ID
 *   }
 *
 * 【与User实体的区别】
 *   User (Entity)     ── 数据库完整映射，含密码、邮箱等全部字段
 *   UserPrincipal     ── 轻量级身份凭证，只含认证必需的3个字段
 *   分离设计避免了Token中携带过多信息，减小JWT体积
 */
@Data
@AllArgsConstructor
public class UserPrincipal {
    /** 用户ID —— 最核心的标识，所有业务操作都基于此判断数据归属 */
    private Long userId;
    /** 用户名 —— 辅助信息，用于日志记录等场景 */
    private String username;
    /** 角色 —— ADMIN/USER，用于权限判断 */
    private String role;

    /**
     * 便捷方法：判断当前用户是否为管理员
     * 用于需要区分管理员和普通用户的业务逻辑
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
