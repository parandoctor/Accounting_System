package com.accounting.system.repository;

import com.accounting.system.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * 预算数据访问层
 * ============================================================
 *
 * 支持两种预算模式：
 * - 分类预算：关联具体categoryId（如"餐饮"每月最多1000）
 * - 总预算：categoryId为null（如每月总支出不超过5000）
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    /** 查询用户某月所有预算记录 */
    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);
    /** 查询用户某月某分类的预算（分类预算） */
    Optional<Budget> findByUserIdAndYearAndMonthAndCategoryId(Long userId, int year, int month, Long categoryId);
    /** 查询用户某月的总预算（categoryId为null） */
    Optional<Budget> findByUserIdAndYearAndMonthAndCategoryIsNull(Long userId, int year, int month);
}
