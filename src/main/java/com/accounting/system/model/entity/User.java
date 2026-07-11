package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 用户实体 —— 系统最核心的领域对象
 * ============================================================
 *
 * 【ORM映射关系】
 * @Entity + @Table("t_user") → 映射到数据库的 t_user 表
 * 字段通过 @Column 注解映射到具体列，JPA自动处理Java类型和SQL类型的转换
 *
 * 【角色与状态设计】
 * Role：
 *   USER  —— 普通用户，只能操作自己的数据
 *   ADMIN —— 管理员，可查看所有用户、禁用/启用账号、重置密码
 *
 * Status：
 *   ACTIVE   —— 正常状态，可以登录和使用系统
 *   DISABLED —— 被管理员禁用，登录时会被拦截
 *
 * 【生命周期回调】
 * @PrePersist：首次保存到数据库之前自动执行，设置创建时间和默认值
 * @PreUpdate：每次更新到数据库之前自动执行，刷新更新时间
 * 这种设计避免了手动设置时间，减少了遗漏的可能
 *
 * 【Builder模式】
 * @Builder 允许链式构建对象：User.builder().username("admin").password("xxx").build()
 * 比 new User() + setXxx() 更清晰、更不可变友好
 */
@Entity
@Table(name = "t_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** 主键，数据库自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名，唯一约束，用于登录 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 密码，BCrypt加密后的密文，永不为明文 */
    @Column(nullable = false, length = 255)
    private String password;

    /** 昵称，显示用，可与用户名不同 */
    @Column(length = 50)
    private String nickname;

    /** 邮箱 */
    @Column(length = 100)
    private String email;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 角色：USER(普通用户)/ADMIN(管理员)，决定权限范围 */
    @Enumerated(EnumType.STRING)  // 存储为字符串而非数字，可读性好
    @Column(nullable = false, length = 20)
    private Role role;

    /** 状态：ACTIVE(启用)/DISABLED(禁用) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** 创建时间，updatable=false 表示一旦创建不可修改 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间，每次更新自动刷新 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        ACTIVE, DISABLED
    }

    /**
     * JPA生命周期回调：首次持久化前自动执行
     * 设置创建时间、更新时间、默认角色和状态
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) role = Role.USER;      // 默认普通用户
        if (status == null) status = Status.ACTIVE; // 默认启用
    }

    /**
     * JPA生命周期回调：更新前自动执行
     * 只刷新更新时间，不影响创建时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
