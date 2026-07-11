package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.dto.PasswordChangeDTO;
import com.accounting.system.model.dto.UserUpdateDTO;
import com.accounting.system.model.entity.User;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.repository.UserRepository;
import com.accounting.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 * 用户服务实现
 * ============================================================
 *
 * 【密码安全】
 * - 修改密码必须验证旧密码（passwordEncoder.matches）
 * - 新密码经过BCrypt加密后存储（passwordEncoder.encode）
 *
 * 【部分更新】
 * updateProfile 只更新非null字段，实现前端可只传要改的字段
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取个人信息
     */
    @Override
    public UserVO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toUserVO(user);
    }

    /**
     * 更新个人信息 —— 部分更新，只改有值的字段
     */
    @Override
    @Transactional
    public UserVO updateProfile(Long userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        user = userRepository.save(user);
        return toUserVO(user);
    }

    /**
     * 修改密码 —— 先验证旧密码，再加密存储新密码
     */
    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private UserVO toUserVO(User user) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt().format(fmt))
                .build();
    }
}
