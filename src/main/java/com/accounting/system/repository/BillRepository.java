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

    Page<Bill> findByUserIdOrderByBillDateDescCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") Bill.BillType type);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = :type "
            + "AND b.billDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(@Param("userId") Long userId,
                                                     @Param("type") Bill.BillType type,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

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

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = 'EXPENSE' "
            + "AND b.category.id = :categoryId AND YEAR(b.billDate) = :year AND MONTH(b.billDate) = :month")
    BigDecimal sumExpenseByUserAndCategoryAndMonth(@Param("userId") Long userId,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND b.type = 'EXPENSE' "
            + "AND YEAR(b.billDate) = :year AND MONTH(b.billDate) = :month")
    BigDecimal sumExpenseByUserAndMonth(@Param("userId") Long userId,
                                         @Param("year") int year,
                                         @Param("month") int month);
}
