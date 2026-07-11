package com.accounting.system.security;

import com.accounting.system.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * ============================================================
 * JWT 认证过滤器 —— 安全链中的第一道关卡
 * ============================================================
 *
 * 【过滤器在Spring Security中的位置】
 *
 *   请求到达
 *     │
 *     ▼
 *   ┌─────────────────────────────────────────┐
 *   │ JwtAuthenticationFilter  ← 本类         │  ← 最先执行
 *   │   检查Header中的Bearer Token            │
 *   │   解析JWT → 恢复用户身份 → 放入上下文    │
 *   └─────────────────────────────────────────┘
 *     │
 *     ▼
 *   ┌─────────────────────────────────────────┐
 *   │ UsernamePasswordAuthenticationFilter    │
 *   │   (传统表单登录过滤器，本项目不使用)      │
 *   └─────────────────────────────────────────┘
 *     │
 *     ▼
 *   ┌─────────────────────────────────────────┐
 *   │ AuthorizationFilter (授权判断)          │
 *   │   根据URL规则检查用户是否有权限访问       │
 *   └─────────────────────────────────────────┘
 *     │
 *     ▼
 *   Controller
 *
 * 【设计要点】
 * 1. 继承 OncePerRequestFilter：确保每个请求只执行一次（即使内部forward也如此）
 * 2. token无效不拦截请求：验证失败时不设置Authentication，让后续授权过滤器
 *    根据URL规则决定是否放行（公开接口放行，受保护接口返回401）
 * 3. 将role包装为ROLE_前缀的GrantedAuthority：
 *    数据库中是 "ADMIN"，Spring Security要求 "ROLE_ADMIN"
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 过滤器的核心逻辑 —— 对每个HTTP请求执行
     *
     * 处理流程：
     * 1. 从请求头中提取 "Bearer <token>"
     * 2. 验证Token有效性（签名+过期时间）
     * 3. 从Token中解析 userId、username、role
     * 4. 构建Spring Security的Authentication对象
     * 5. 存入SecurityContextHolder（ThreadLocal隔离，线程安全）
     * 6. 调用 filterChain.doFilter() 继续过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // ── 步骤1：从请求中提取JWT Token ──
        String token = extractToken(request);

        // ── 步骤2：Token存在且有效，则进行认证 ──
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // ── 步骤3：解析Token中的用户信息 ──
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // ── 步骤4：构建认证主体对象 ──
            // UserPrincipal：自定义的认证主体，携带userId/username/role
            UserPrincipal principal = new UserPrincipal(userId, username, role);

            // UsernamePasswordAuthenticationToken：
            //   参数1：principal（认证主体）
            //   参数2：credentials（密码，JWT模式不需要，设为null）
            //   参数3：authorities（权限列表，由role转换而来）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal, null,
                            // Spring Security要求权限格式为 "ROLE_XXX"
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            // ── 步骤5：将认证信息存入安全上下文（ThreadLocal） ──
            // 此后，当前请求的任何地方都能通过 SecurityContextHolder 获取用户信息
            // Controller中通过 @AuthenticationPrincipal 直接注入
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ── 步骤6：继续过滤器链（无论认证成功与否，都继续） ──
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求头中提取JWT Token
     *
     * 标准格式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * 提取逻辑：检查是否以 "Bearer " 开头，截取后面部分
     *
     * @param request HTTP请求
     * @return JWT Token字符串，若无则返回null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 共7个字符，从第7位开始截取
            return bearerToken.substring(7);
        }
        return null;
    }
}
