package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.dto.LoginDTO;
import com.accounting.system.model.dto.RegisterDTO;
import com.accounting.system.model.entity.User;
import com.accounting.system.model.vo.LoginVO;
import com.accounting.system.repository.UserRepository;
import com.accounting.system.security.JwtTokenProvider;
import com.accounting.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================
 * 认证服务实现 —— 注册与登录的核心逻辑
 * ============================================================
 *
 * 【安全设计要点】
 * 1. 密码绝不明文存储：BCrypt加密，每次加密结果不同（内置随机盐值）
 * 2. 登录模糊提示："用户名或密码错误"而非"密码错误"
 *    （防止攻击者通过错误信息枚举有效用户名）
 * 3. 账号禁用检查：登录时校验status
 *
 * 【@Transactional 使用说明】
 * register方法标注了@Transactional：
 * - 保证原子性：userRepository.save()失败不会留下脏数据
 * - login方法未标注：纯查询操作，不需要事务
 *
 * 【v1.0.1 日志增强】
 * 关键操作（注册、登录、登录失败）均记录结构化日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册
     *
     * 流程：
     * 1. 检查用户名是否已存在（唯一性校验）
     * 2. BCrypt加密密码
     * 3. 构建User实体，设置默认角色(USER)和状态(ACTIVE)
     * 4. 保存到数据库
     *
     * @throws BusinessException 用户名已存在时抛出
     */
    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername())
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", dto.getUsername());
    }

    /**
     * 用户登录
     *
     * 流程（阶梯式校验，失败即止）：
     * 1. 根据用户名查用户 → 不存在则抛异常
     * 2. 检查账号状态 → 被禁用则抛异常
     * 3. BCrypt比对密码 → 不匹配则抛异常
     * 4. 全部通过 → 签发JWT Token（有效期24小时）
     *
     * @return LoginVO 包含Token和用户基本信息
     * @throws BusinessException 用户名不存在/密码错误/账号被禁用
     */
    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found - {}", dto.getUsername());
                    return new BusinessException("用户名或密码错误");
                });

        if (user.getStatus() == User.Status.DISABLED) {
            log.warn("Login attempt by disabled user: {}", dto.getUsername());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed: incorrect password for user {}", dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        log.info("User logged in: {}", dto.getUsername());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }
}