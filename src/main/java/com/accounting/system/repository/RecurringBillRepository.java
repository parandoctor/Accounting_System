package com.accounting.system.repository;

import com.accounting.system.model.entity.RecurringBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringBillRepository extends JpaRepository<RecurringBill, Long> {
    List<RecurringBill> findByUserIdAndIsActiveTrue(Long userId);
    List<RecurringBill> findByIsActiveTrueAndNextDateLessThanEqual(LocalDate date);
}
