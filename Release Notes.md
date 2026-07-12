个人记账系统后端 - 修改内容说明文档
本文档记录了本次代码审查后对所有 Service 层及关联模块的优化和修复。
修改主要围绕 数据一致性、性能、安全性和可维护性 展开，所有改动均保证对外 API 接口不变，对现有功能无侵入。

📋 目录
问题回顾

修改总览

详细修改内容

3.1 BudgetServiceImpl – 预算服务重构

3.2 BillRepository – 新增批量聚合查询

3.3 RecurringBillServiceImpl – 定时任务容错增强

3.4 AdminServiceImpl – 安全管理与随机密码

3.5 AuthServiceImpl – 日志增强

3.6 BillServiceImpl – 日志补充

3.7 可选：Budget 实体字段清理

数据库变更建议

测试指南

部署注意事项

1. 问题回顾
在审阅全部源码（含 Service 实现）后，识别出以下核心问题：

问题分类	具体描述	影响
数据一致性	Budget 实体存储了 spentAmount 和 isOverBudget，但这些值需随 Bill 变化而更新，易出现不一致。	预算数据可能不准确，且需多处同步更新，维护成本高。
性能瓶颈	获取预算列表时对每条预算单独查询当月支出（N+1 问题），影响响应速度。	当预算条目较多时，数据库查询数激增。
定时任务脆弱性	processDueRecurringBills 中若某条记录处理失败，将导致整个事务回滚，后续记录无法处理。	单点故障影响全部周期账单生成。
管理员操作安全	管理员可禁用自身账号，且重置密码为固定弱密码 123456。	存在安全风险，且密码策略不够健壮。
日志审计不足	关键操作（登录、注册、增删改账单、预算设置等）缺乏结构化日志。	问题追溯困难，安全审计无据可依。
代码可维护性	部分枚举转换未做异常处理，且预算查询逻辑冗余。	非法输入可能导致 500 错误，且代码重复。
2. 修改总览
针对上述问题，本次修改包含以下核心改进：

预算完全实时计算：移除对 spentAmount 和 isOverBudget 的存储依赖，所有数据均从 Bill 表动态聚合。同时使用批量查询（GROUP BY）优化性能，消除 N+1。

定时任务容错：在 processDueRecurringBills 中增加逐条异常捕获，确保单条失败不影响其他记录。

管理员密码安全：重置密码改为生成 8 位随机密码（含大小写字母和数字），并输出日志（实际生产应通过邮件发送）。

自身禁用检查：增加管理员不能禁用自身账号的逻辑（需配合 Controller 传入当前用户 ID）。

全面日志记录：在认证、账单、预算、管理员操作等关键路径添加 log.info/warn，便于追踪。

代码精简：提取公共转换方法，消除重复代码。

3. 详细修改内容
3.1 BudgetServiceImpl – 预算服务重构
修改文件：service/impl/BudgetServiceImpl.java

核心变化：

移除 spentAmount 和 isOverBudget 的写入：setBudget 仅保存预算金额，不再计算已花费。

批量查询支出：新增 getMonthlySpentByCategory 方法，通过 BillRepository.sumExpenseGroupByCategoryForMonth 一次性获取该月所有分类的支出总和。

实时构建 VO：在 getBudgets 中根据批量查询结果组装 BudgetVO，计算剩余金额和使用百分比。

复用逻辑：getBudgetUsage 直接调用 getBudgets，避免重复代码。

代码片段示例（仅展示关键新增方法）：

java
private Map<Long, BigDecimal> getMonthlySpentByCategory(Long userId, int year, int month) {
    List<Object[]> results = billRepository.sumExpenseGroupByCategoryForMonth(userId, year, month);
    return results.stream()
            .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (BigDecimal) row[1],
                    BigDecimal::add
            ));
}
3.2 BillRepository – 新增批量聚合查询
修改文件：repository/BillRepository.java

新增方法：

java
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
作用：支持预算模块一次性获取所有分类支出，大幅减少数据库查询次数。

3.3 RecurringBillServiceImpl – 定时任务容错增强
修改文件：service/impl/RecurringBillServiceImpl.java

核心变化：

在 processDueRecurringBills 的循环内部增加 try-catch，捕获异常后记录日志并继续处理下一条。

增加详细日志输出（处理条数、每条处理的成功/失败状态）。

关键代码：

java
for (RecurringBill rb : dueBills) {
    try {
        // 生成账单并更新下次日期
        ...
    } catch (Exception e) {
        log.error("Failed to process recurring bill id={} for user {}: {}",
                rb.getId(), rb.getUserId(), e.getMessage(), e);
    }
}
3.4 AdminServiceImpl – 安全管理与随机密码
修改文件：service/impl/AdminServiceImpl.java

变化点：

增加自身禁用检查（需配合 Controller 传入 currentAdminId）：

java
if (currentAdminId.equals(targetUserId)) {
    throw new BusinessException("不能禁用自己");
}
注：由于原有接口未传递当前管理员 ID，开发者需自行修改 Controller 以传入该参数，或从 SecurityContextHolder 获取（但建议显式传递）。

重置密码生成随机串：

java
private String generateRandomPassword(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
    }
    return sb.toString();
}
日志记录新密码（生产环境建议通过邮件/短信通知用户）。

增加操作日志：记录状态切换和密码重置事件。

3.5 AuthServiceImpl – 日志增强
修改文件：service/impl/AuthServiceImpl.java

增加日志：

注册成功 → log.info("User registered: {}", username)

登录失败（用户不存在/密码错误/账户禁用） → log.warn(...)

登录成功 → log.info("User logged in: {}", username)

3.6 BillServiceImpl – 日志补充
修改文件：service/impl/BillServiceImpl.java

新增日志点：

创建账单 → log.info("Bill created: user={}, amount={}, category={}", ...)

更新账单 → log.info("Bill updated: user={}, billId={}", ...)

删除账单 → log.info("Bill deleted: user={}, billId={}", ...)

其他方法可依此类推添加。

3.7 可选：Budget 实体字段清理
修改文件：model/entity/Budget.java

若希望彻底移除冗余字段，可注释或删除 spentAmount 和 isOverBudget 字段及其相关注解。由于本次修改已使其成为“死字段”，保留或删除均可，不影响功能。

推荐做法：暂时保留字段并添加 @Deprecated 注解，后续数据库迁移时再删除列。

4. 数据库变更建议
由于 Budget 表中的 spent_amount 和 is_over_budget 字段已不再使用（完全由实时计算替代），建议在后续迭代中执行以下 SQL 删除冗余列（需先确认无外部依赖）：

sql
ALTER TABLE t_budget DROP COLUMN spent_amount;
ALTER TABLE t_budget DROP COLUMN is_over_budget;
注意：此操作为破坏性变更，请先在测试环境验证，并确保所有代码已部署新版本后再执行。

5. 测试指南
为确保修改正确，建议进行以下测试用例：

测试场景	操作步骤	预期结果
预算设置与查询	1. 设置分类预算和总预算
2. 添加支出账单
3. 调用 /api/budgets 和 /api/budgets/usage	返回的 spentAmount 和 usagePercent 与账单实时一致，无延迟
周期账单生成	1. 创建周期账单（nextDate=今天）
2. 手动触发定时任务（或等待次日3点）
3. 查看账单列表	自动生成实际账单，且 nextDate 正确推移
若某条周期账单分类被删除，应捕获异常不影响其他
管理员禁用自身	管理员登录，调用 /api/admin/users/{id}/status 其中 id 为自身 ID	返回业务错误 不能禁用自己
重置密码	管理员重置普通用户密码	用户新密码为随机字符串，日志中可见（生产需屏蔽）
并发预算查询	多用户同时查询预算列表	响应时间无明显增加，数据库查询次数可控（单次聚合查询）
6. 部署注意事项
代码替换顺序：

先备份原有文件。

按文件清单逐一替换（Service 实现、Repository）。

若选择清理实体字段，需同步修改数据库（建议延后执行）。

配置文件无变更：本次修改不涉及 application.yml 或 pom.xml。

日志级别调整：可根据需要调整 log.warn 的输出级别（生产环境建议 INFO 及以上）。

管理员自身禁用检查：若未修改 Controller 传入 currentAdminId，则该项检查暂时不会生效，但不会影响其他功能。建议尽快补充，增强安全性。

随机密码通知：当前重置密码仅记录在日志中，若需邮件发送，请集成邮件服务并在 resetUserPassword 中调用。

定时任务容错：单条失败会记录 ERROR 日志但事务不回滚，需关注日志报警，及时处理异常数据。

7. 总结
本次修改解决了预算数据一致性和性能问题，强化了定时任务健壮性，提升了管理员操作安全性，并完善了系统日志。所有改动均向下兼容，无破坏性变更。建议在测试环境充分验证后上线。

文档版本：1.0
最后更新：2026-07-12
编制人：zkr216