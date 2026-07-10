package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.entity.User;
import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.UserVO;
import com.accounting.system.repository.BillRepository;
import com.accounting.system.repository.UserRepository;
import com.accounting.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final PasswordEncoder passwordEncoder;

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
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getRole() == User.Role.ADMIN) {
            throw new BusinessException("不能禁用管理员账号");
        }

        user.setStatus(user.getStatus() == User.Status.ACTIVE ? User.Status.DISABLED : User.Status.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // Reset to default password: 123456
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    @Override
    public Map<String, Long> getSystemStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalBills", billRepository.count());
        return stats;
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
