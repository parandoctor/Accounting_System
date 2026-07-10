package com.accounting.system.repository;

import com.accounting.system.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);
    Optional<Budget> findByUserIdAndYearAndMonthAndCategoryId(Long userId, int year, int month, Long categoryId);
    Optional<Budget> findByUserIdAndYearAndMonthAndCategoryIsNull(Long userId, int year, int month);
}
