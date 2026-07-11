package com.accounting.system.repository;

import com.accounting.system.model.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * 账单数据访问层 —— Spring Data JPA 的威力展示
 * ============================================================
 *
 * 【Spring Data JPA 的核心优势】
 * 开发者只需定义接口，无需写实现类！框架在运行时自动生成代理实现。
 * 方法名即查询语义（方法命名约定）：
 *   findByUserIdOrderByBillDateDesc → WHERE user_id = ? ORDER BY bill_date DESC
 *
 * 【本接口集成了两种查询机制】
 * 1. JpaRepository<Bill, Long>         —— 基础CRUD + 方法命名查询
 * 2. JpaSpecificationExecutor<Bill>    —— 动态条件查询（灵活但复杂）
 *
 * 【为什么同时用两种？】
 * - 简单查询用方法命名：findByUserId，简单直观
 * - 复杂查询用@Query：sumByCategory（多表关联+聚合+分组）
 * - 动态多条件查询用Specification：listBills中用户可能传type/categoryId/startDate等
 *   任意组合的条件，不可能为每种组合都写方法
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    /**
     * 按用户分页查询，按日期和创建时间倒序
     * Spring Data自动解析方法名生成SQL：
     *   SELECT * FROM t_bill WHERE user_id = ? ORDER BY bill_date DESC, created_at DESC
     */
    Page<Bill> findByUserIdOrderByBillDateDescCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计用户某种类型的总金额（收入或支出总计）
     * 
     * JPQL要点：
     * - COALESCE(SUM(...), 0)：当没有记录时返回0而非null，避免NPE
     * - :userId：命名参数，通过@Param绑定
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") Bill.BillType type);

    /**
     * 统计用户在指定日期范围内的某类型总金额
     * 用于收支趋势图、月度统计等场景
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = :type "
            + "AND b.billDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(@Param("userId") Long userId,
                                                     @Param("type") Bill.BillType type,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 按分类汇总 —— 收支统计图表的核心查询
     * 
     * 返回 Object[] 列表，每个数组包含：
     *   [0] categoryId    Long    分类ID
     *   [1] categoryName  String  分类名称
     *   [2] categoryIcon  String  分类图标
     *   [3] type          BillType 收支类型
     *   [4] amount        BigDecimal 汇总金额
     *   [5] count         Long    账单笔数
     * 
     * 支持可选的日期范围过滤：startDate/endDate为null时不限制
     */
    @Query("SELECT b.category.id, c.name, c.icon, b.type, SUM(b.amount), COUNT(b) "
            + "FROM Bill b JOIN b.category c "
            + "WHERE b.userId = :userId "
            + "AND (:startDate IS NULL OR b.billDate >= :startDate) "
            + "AND (:endDate IS NULL OR b.billDate <= :endDate) "
            + "GROUP BY b.category.id, c.name, c.icon, b.type "
            + "ORDER BY SUM(b.amount) DESC")
    List<Object[]> sumByCategory(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /** 统计用户的账单总数（管理员统计用） */
    long countByUserId(Long userId);

    /**
     * 统计用户某月某分类的支出总额
     * 用于预算追踪：对比预算金额与实际支出
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = 'EXPENSE' "
            + "AND b.category.id = :categoryId AND YEAR(b.billDate) = :year AND MONTH(b.billDate) = :month")
    BigDecimal sumExpenseByUserAndCategoryAndMonth(@Param("userId") Long userId,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);

    /**
     * 统计用户某月总支出（不区分分类）
     * 用于总预算追踪
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = 'EXPENSE' "
            + "AND YEAR(b.billDate) = :year AND MONTH(b.billDate) = :month")
    BigDecimal sumExpenseByUserAndMonth(@Param("userId") Long userId,
                                         @Param("year") int year,
                                         @Param("month") int month);
}
