package com.accounting.system.repository;

import com.accounting.system.model.entity.RecurringBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * 周期账单数据访问层
 * ============================================================
 *
 * 核心查询：
 * - 用户查看自己的周期账单：findByUserIdAndIsActiveTrue
 * - 定时任务查找到期账单：findByIsActiveTrueAndNextDateLessThanEqual
 *   每天凌晨3点执行，找出所有nextDate <= 今天的活跃周期账单
 */
@Repository
public interface RecurringBillRepository extends JpaRepository<RecurringBill, Long> {
    /** 查询用户的活跃周期账单（排除软删除的） */
    List<RecurringBill> findByUserIdAndIsActiveTrue(Long userId);
    /** 查询所有到期需要执行的周期账单（定时任务使用，不限于特定用户） */
    List<RecurringBill> findByIsActiveTrueAndNextDateLessThanEqual(LocalDate date);
}
