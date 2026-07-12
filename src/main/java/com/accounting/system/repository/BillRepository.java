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

@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    // ... 已有方法保持不变 ...

    /**
     * 新增：统计用户某月各分类的支出总额（用于预算批量计算）
     * 返回 List<Object[]>，每个元素 [categoryId, totalAmount]
     */
    @Query("SELECT b.category.id, COALESCE(SUM(b.amount), 0) " +
           "FROM Bill b " +
           "WHERE b.userId = :userId " +
           "AND b.type = 'EXPENSE' " +
           "AND YEAR(b.billDate) = :year " +
           "AND MONTH(b.billDate) = :month " +
           "GROUP BY b.category.id")
    List<Object[]> sumExpenseGroupByCategoryForMonth(@Param("userId") Long userId,
                                                     @Param("year") int year,
                                                     @Param("month") int month);

    // 其他方法 ...
}