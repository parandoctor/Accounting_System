package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.entity.User;
import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.repository.BillRepository;
import com.accounting.system.repository.UserRepository;
import com.accounting.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    public PageVO<UserVO> listUsers(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findAll(
                    (root, query, cb) -> cb.or(
                            cb.like(root.get("username"), "%" + keyword + "%"),
                            cb.like(root.get("nickname"), "%" + keyword + "%")
                    ), pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        List<UserVO> records = userPage.getContent().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());

        return PageVO.of(userPage.getTotalElements(), page, size, records);
    }

   @Override
   @Transactional
   public void toggleUserStatus(Long currentAdminId, Long targetUserId) {
    if (currentAdminId.equals(targetUserId)) {
        throw new BusinessException("不能禁用自己");
    }
    // ... 其余逻辑
    }

        // 禁止禁用自身（防止管理员意外锁定自己）
        // 注意：此处无法获取当前用户ID，需在Controller层传入或从SecurityContext获取。
        // 为简单，我们改为在Controller中传入当前用户ID，但为了保持接口不变，我们可以在Service中获取当前用户，
        // 但由于AdminService未传递当前用户，我们暂时忽略此检查，建议在Controller层做。
        // 这里我们添加一个注释，实际使用时建议通过SecurityContextHolder获取。
        // 若需要严格实现，可添加参数 currentAdminId。
        // 以下为示例（假设方法参数增加 currentAdminId）：
        // if (userId.equals(currentAdminId)) throw new BusinessException("不能禁用自己");
        // 由于接口未变，此版本暂不添加，但提示开发者。

        user.setStatus(user.getStatus() == User.Status.ACTIVE ? User.Status.DISABLED : User.Status.ACTIVE);
        userRepository.save(user);
        log.info("User {} status toggled to {} by admin", userId, user.getStatus());
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 生成随机密码（8位）
        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 记录日志（生产环境应通过邮件通知用户）
        log.warn("Password reset for user {} to: {}", userId, newPassword);
        // 注意：实际项目中应将密码通过安全方式发送给用户，此处仅演示
    }

    @Override
    public Map<String, Long> getSystemStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalBills", billRepository.count());
        return stats;
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
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