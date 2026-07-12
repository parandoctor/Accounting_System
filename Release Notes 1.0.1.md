# 个人记账系统 v1.0.1 发布说明

> 基于 v1.0 的优化增强版本，修复性能瓶颈、增强安全性和可观测性。

---

## 📋 版本信息

| 项目 | 内容 |
|------|------|
| 版本号 | 1.0.1 |
| 发布日期 | 2026-07-12 |
| 基于版本 | 1.0.0 |
| 兼容性 | 向下兼容，API 接口无变更 |

---

## 🎯 修改概览

本次版本在 v1.0 基础上，针对代码审查中发现的 **数据一致性**、**性能瓶颈**、**安全性** 和 **可观测性** 四方面问题进行了集中优化。

| 模块 | 改动类型 | 说明 |
|------|----------|------|
| `BudgetServiceImpl` | 🔄 重构 | 预算支出改为批量聚合实时计算，消除 N+1 查询 |
| `BillRepository` | ➕ 新增 | 新增 `sumExpenseGroupByCategoryForMonth` 批量聚合方法 |
| `RecurringBillServiceImpl` | 🔧 增强 | 定时任务增加逐条 try-catch 容错 |
| `AdminServiceImpl` | 🔧 增强 | 随机密码替代固定密码；增加自禁用检查 |
| `AdminService` | ⚠️ 签名变更 | `toggleUserStatus` 新增 `currentAdminId` 参数 |
| `AdminController` | 🔧 增强 | 注入 `@AuthenticationPrincipal` 传递当前管理员 ID |
| `AuthServiceImpl` | ➕ 增强 | 增加 `@Slf4j` 结构化日志 |
| `BillServiceImpl` | ➕ 增强 | 增加 `@Slf4j` 关键操作日志 |
| `Budget.java` | 📝 文档 | 更新 Javadoc 描述 v1.0.1 追踪机制 |

---

## 📝 详细修改

### 1. BudgetServiceImpl — 预算实时计算重构

**问题**：原实现对每条预算单独查询当月支出，存在 N+1 查询问题；`spentAmount` 存储值需在多处同步更新，容易数据不一致。

**方案**：
- 所有支出数据从 Bill 表实时聚合计算，不再依赖存储的 `spentAmount`
- 新增 `getMonthlySpentByCategory()` 方法，通过 `BillRepository.sumExpenseGroupByCategoryForMonth` 一次性获取所有分类支出
- `getBudgetUsage` 直接复用 `getBudgets` 逻辑，消除重复代码

**影响**：预算查询从 N+1 次数据库查询降为 1 次；数据始终与账单表一致。

### 2. BillRepository — 新增批量聚合查询

新增方法 `sumExpenseGroupByCategoryForMonth`：

```java
@Query("SELECT b.category.id, COALESCE(SUM(b.amount), 0) "
     + "FROM Bill b "
     + "WHERE b.userId = :userId AND b.type = 'EXPENSE' "
     + "AND YEAR(b.billDate) = :year AND MONTH(b.billDate) = :month "
     + "GROUP BY b.category.id")
List<Object[]> sumExpenseGroupByCategoryForMonth(...);
```

### 3. RecurringBillServiceImpl — 定时任务容错

**问题**：`processDueRecurringBills` 中若某条周期账单处理失败（如分类被删除），整个事务回滚，后续记录无法处理。

**方案**：循环内增加 `try-catch`，捕获异常后记录 ERROR 日志并继续处理下一条。

```java
for (RecurringBill rb : dueBills) {
    try {
        // 生成账单并更新下次日期
    } catch (Exception e) {
        log.error("Failed to process recurring bill id={}: {}", rb.getId(), e.getMessage());
    }
}
```

### 4. AdminServiceImpl — 安全管理增强

- **密码安全**：重置密码从固定 `123456` 改为 8 位随机密码（大写+小写+数字），通过 `SecureRandom` 生成
- **自禁用保护**：`toggleUserStatus` 增加 `currentAdminId` 参数，禁止管理员禁用自身
- **操作审计**：增加 `@Slf4j` 日志，记录状态切换和密码重置事件

### 5. AdminService / AdminController — 接口适配

- `AdminService.toggleUserStatus` 签名变更为 `(Long currentAdminId, Long targetUserId)`
- `AdminController` 通过 `@AuthenticationPrincipal UserPrincipal` 获取当前管理员 ID 并传入

### 6. 全面日志增强

| 文件 | 新增日志点 |
|------|-----------|
| `AuthServiceImpl` | 注册成功/登录成功(info)、登录失败(warn) |
| `BillServiceImpl` | 创建/更新/删除账单(info) |
| `BudgetServiceImpl` | 预算设置(info) |
| `RecurringBillServiceImpl` | 周期账单创建/删除(info)、定时任务处理(info/error) |
| `AdminServiceImpl` | 状态切换(info)、密码重置(warn) |

---

## 🔄 兼容性说明

- **API 接口**：无变更，对外路径和参数保持一致
- **数据库**：无需迁移，`t_budget` 表的 `spent_amount` 和 `is_over_budget` 列保留（查询时优先实时计算）
- **配置文件**：无变更

---

## 🧪 测试建议

| 场景 | 验证点 |
|------|--------|
| 预算查询 | 添加支出后查询预算，`spentAmount` 和 `usagePercent` 应与实时账单一致 |
| 周期账单生成 | 某条周期账单关联的分类被删除后，定时任务不中断，其他记录正常生成 |
| 管理员自禁用 | 管理员禁用自身 ID 应返回 "不能禁用自己的账号" |
| 密码重置 | 重置后日志输出随机密码，用户可用新密码登录 |
| 并发查询 | 多用户同时查询预算，响应时间无明显增加 |

---

## 📦 文件变更清单

| 文件 | 变更 |
|------|------|
| `model/entity/Budget.java` | 更新 Javadoc |
| `repository/BillRepository.java` | 新增 `sumExpenseGroupByCategoryForMonth` |
| `service/AdminService.java` | `toggleUserStatus` 签名变更 |
| `service/impl/AdminServiceImpl.java` | 重写：随机密码、自禁用检查、日志 |
| `service/impl/AuthServiceImpl.java` | 增加 Javadoc + @Slf4j 日志 |
| `service/impl/BillServiceImpl.java` | 增加 @Slf4j 日志 |
| `service/impl/BudgetServiceImpl.java` | 重构：批量聚合计算 |
| `service/impl/RecurringBillServiceImpl.java` | 增加 Javadoc + try-catch 容错 |
| `controller/AdminController.java` | 注入 UserPrincipal，修复 import 和缩进 |

---

> **文档版本**：1.0.1  
> **编制人**：zkr216  
> **最后更新**：2026-07-12
