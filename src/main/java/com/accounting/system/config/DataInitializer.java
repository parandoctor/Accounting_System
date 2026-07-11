package com.accounting.system.config;

import com.accounting.system.model.entity.Category;
import com.accounting.system.model.entity.User;
import com.accounting.system.repository.CategoryRepository;
import com.accounting.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================
 * 系统数据初始化器
 * ============================================================
 *
 * 【作用说明】
 * 实现 CommandLineRunner 接口，在Spring容器启动完成后自动执行。
 * 用于初始化系统运行所必需的基础数据，保证系统"开箱即用"。
 *
 * 【执行时机】
 * Spring Boot启动流程：
 *   main() → SpringApplication.run() → 创建IoC容器 → 自动配置 → 
 *   → 执行所有 CommandLineRunner（本类在此） → 应用就绪
 *
 * 【幂等性保证】
 * 所有初始化方法都先检查数据是否已存在：
 * - 分类表：count() > 0 则跳过
 * - 管理员用户：findByUsername("admin") 存在则跳过
 * 这样多次重启不会重复插入数据。
 *
 * 【初始化的数据】
 * 1. 18个收支分类（12个支出 + 6个收入），每个都有emoji图标和排序
 * 2. 默认管理员账号 admin/admin123
 */
@Slf4j              // Lombok：自动生成log对象，用于日志输出
@Component          // Spring组件注解，让IoC容器管理此Bean
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 启动时入口：依次执行分类初始化和管理员初始化
     */
    @Override
    public void run(String... args) {
        initCategories();
        initAdminUser();
    }

    /**
     * 初始化收支分类
     * 
     * 设计要点：
     * - sortOrder 用于前端排序展示，数字越小越靠前
     * - 99 作为"其他"分类的排序值，确保排在最末
     * - icon 使用 emoji 字符，前端可直接渲染，无需额外图标库
     * - 分为 EXPENSE(支出) 和 INCOME(收入) 两大类
     */
    private void initCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
            // ── 支出分类（12个）──
            createCategory("餐饮", Category.CategoryType.EXPENSE, "🍔", 1),
            createCategory("购物", Category.CategoryType.EXPENSE, "🛒", 2),
            createCategory("交通", Category.CategoryType.EXPENSE, "🚗", 3),
            createCategory("住房", Category.CategoryType.EXPENSE, "🏠", 4),
            createCategory("娱乐", Category.CategoryType.EXPENSE, "🎮", 5),
            createCategory("医疗", Category.CategoryType.EXPENSE, "💊", 6),
            createCategory("教育", Category.CategoryType.EXPENSE, "📚", 7),
            createCategory("通讯", Category.CategoryType.EXPENSE, "📱", 8),
            createCategory("服饰", Category.CategoryType.EXPENSE, "👔", 9),
            createCategory("运动", Category.CategoryType.EXPENSE, "⚽", 10),
            createCategory("零食", Category.CategoryType.EXPENSE, "🍿", 11),
            createCategory("其他支出", Category.CategoryType.EXPENSE, "💸", 99),
            // ── 收入分类（6个）──
            createCategory("工资", Category.CategoryType.INCOME, "💰", 1),
            createCategory("奖金", Category.CategoryType.INCOME, "🎁", 2),
            createCategory("兼职", Category.CategoryType.INCOME, "💼", 3),
            createCategory("投资收益", Category.CategoryType.INCOME, "📈", 4),
            createCategory("退款", Category.CategoryType.INCOME, "↩️", 5),
            createCategory("其他收入", Category.CategoryType.INCOME, "📥", 99)
        );

        categoryRepository.saveAll(categories);
        log.info("Initialized {} categories", categories.size());
    }

    /**
     * 初始化管理员账号
     * 默认密码 "admin123" 通过BCrypt加密后存储，数据库中不会保存明文
     */
    private void initAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) return;

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))  // BCrypt加密
                .nickname("系统管理员")
                .role(User.Role.ADMIN)      // 管理员角色，拥有最高权限
                .status(User.Status.ACTIVE) // 默认启用
                .build();

        userRepository.save(admin);
        log.info("Initialized admin user: admin/admin123");
    }

    /**
     * 工厂方法：快速构建Category实体
     */
    private Category createCategory(String name, Category.CategoryType type, String icon, int sortOrder) {
        return Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .sortOrder(sortOrder)
                .build();
    }
}
