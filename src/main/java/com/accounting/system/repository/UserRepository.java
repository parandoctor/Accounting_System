package com.accounting.system.repository;

import com.accounting.system.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * ============================================================
 * 用户数据访问层
 * ============================================================
 *
 * 【命名约定方法】
 * findByUsername：Spring Data自动解析为 SELECT * FROM t_user WHERE username = ?
 * existsByUsername：SELECT COUNT(*) > 0 FROM t_user WHERE username = ?
 *   Spring Data 通过方法名中的 findBy/existsBy 关键字区分查询类型
 *
 * 【Optional 的作用】
 * 返回 Optional<User> 而非 User：
 * - 强制调用方处理"用户不存在"的情况
 * - 避免 NullPointerException
 * - 用法：userRepository.findByUsername("admin").orElseThrow(() -> new BusinessException("用户不存在"))
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /** 根据用户名查询用户（登录时使用） */
    Optional<User> findByUsername(String username);
    /** 检查用户名是否已存在（注册时查重） */
    boolean existsByUsername(String username);
}
