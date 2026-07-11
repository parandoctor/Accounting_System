package com.accounting.system.config;

import com.accounting.system.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * Spring Security 安全配置
 * ============================================================
 *
 * 【配置职责】
 * 本类是系统安全的核心配置，负责：
 * 1. 定义哪些URL需要认证、哪些可以匿名访问（授权规则）
 * 2. 配置无状态会话策略（JWT模式下不需要Session）
 * 3. 注入自定义的JWT认证过滤器
 * 4. 注册BCrypt密码编码器Bean
 *
 * 【安全机制原理】
 * 整个安全流程如下：
 *
 *   客户端请求
 *     │
 *     ▼
 *   JwtAuthenticationFilter（最先执行）
 *     │  从 Header 中提取 "Bearer <token>"
 *     │  验证JWT签名 → 解析userId/username/role
 *     │  构造 Authentication 对象放入 SecurityContext
 *     ▼
 *   SecurityFilterChain（授权判断）
 *     │  /api/auth/**        → permitAll（登录注册无需认证）
 *     │  /api/admin/**       → hasRole("ADMIN")（仅管理员）
 *     │  /api/**             → authenticated（需要登录）
 *     ▼
 *   Controller 层处理请求
 *
 * 【无状态设计要点】
 * SessionCreationPolicy.STATELESS：
 * - 服务器不创建HTTP Session
 * - 每次请求都携带JWT，服务端通过解析Token来识别用户身份
 * - 这使得系统天然支持水平扩展（无状态 = 无Session同步问题）
 */
@Configuration          // 标识为Spring配置类
@EnableWebSecurity      // 启用Spring Security的Web安全支持
@RequiredArgsConstructor // Lombok：为final字段生成构造函数注入
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 注册BCrypt密码编码器
     * BCrypt是一种自适应哈希算法，内置盐值(salt)，能有效抵御彩虹表攻击
     * 强度因子默认为10（2^10轮哈希），兼顾安全性和性能
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链 —— Spring Security的核心配置
     *
     * 相比旧版继承 WebSecurityConfigurerAdapter，新版使用注入Bean方式，
     * 通过 HttpSecurity 的流式API进行声明式配置，更清晰、更安全。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护：前后端分离 + JWT令牌天然防CSRF，无需此机制
            .csrf(AbstractHttpConfigurer::disable)
            // 设置为无状态会话：不创建Session，每次请求都通过JWT认证
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // URL授权规则配置（从上到下匹配，先匹配先生效）
            .authorizeHttpRequests(auth -> auth
                // ① 认证接口 —— 注册/登录，无需携带Token
                .requestMatchers("/api/auth/**").permitAll()
                // ② 管理员接口 —— 必须具有ROLE_ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // ③ 其他API —— 任何已认证用户均可访问
                .requestMatchers("/api/**").authenticated()
                // ④ 其余请求（如静态资源）放行
                .anyRequest().permitAll()
            )
            // 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
            // 这样JWT过滤器先执行，从Token中恢复用户身份后，Spring Security的授权机制才能正常工作
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
