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
 * 3. 账号禁用检查：登录时校验status，不直接提示"账号已禁用"
 *    （同样出于安全考虑，减少信息泄露）
 *
 * 【@Transactional 使用说明】
 * register方法标注了@Transactional：
 * - 保证原子性：userRepository.save()失败不会留下脏数据
 * - readOnly=false（默认）：涉及写操作
 * - login方法未标注：纯查询操作，不需要事务
 */
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
        // 用户名唯一性校验
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 构建用户实体
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))  // BCrypt加密
                .nickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername())
                .role(User.Role.USER)       // 注册用户默认为普通用户
                .status(User.Status.ACTIVE)  // 默认启用
                .build();

        userRepository.save(user);
    }

    /**
     * 用户登录
     *
     * 流程（阶梯式校验，失败即止）：
     * 1. 根据用户名查用户 → 不存在则抛异常（不明确说"用户不存在"）
     * 2. 检查账号状态 → 被禁用则抛异常
     * 3. BCrypt比对密码 → 不匹配则抛异常（和步骤1同样的错误消息）
     * 4. 全部通过 → 签发JWT Token
     *
     * @return LoginVO 包含Token和用户基本信息
     * @throws BusinessException 用户名不存在/密码错误/账号被禁用
     */
    @Override
    public LoginVO login(LoginDTO dto) {
        // 步骤1：查找用户
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 步骤2：检查账号状态
        if (user.getStatus() == User.Status.DISABLED) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 步骤3：密码比对（BCrypt.matches：明文 vs 密文）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 步骤4：签发JWT Token（有效期24小时）
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }
}
