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

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initCategories();
        initAdminUser();
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
            // 支出分类
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
            // 收入分类
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

    private void initAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) return;

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("系统管理员")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Initialized admin user: admin/admin123");
    }

    private Category createCategory(String name, Category.CategoryType type, String icon, int sortOrder) {
        return Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .sortOrder(sortOrder)
                .build();
    }
}
