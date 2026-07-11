package com.accounting.system.repository;

import com.accounting.system.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ============================================================
 * 分类数据访问层
 * ============================================================
 *
 * 分类是系统的字典数据，由 DataInitializer 初始化。
 * 提供按类型筛选和全量查询两种方式。
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /** 按类型查询分类（如只查支出分类），按排序字段升序 */
    List<Category> findByTypeOrderBySortOrder(Category.CategoryType type);
    /** 查询所有分类，按排序字段升序 */
    List<Category> findAllByOrderBySortOrder();
}
