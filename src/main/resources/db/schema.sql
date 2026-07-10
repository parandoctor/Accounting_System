-- ============================================
-- 个人记账系统 - 数据库初始化脚本
-- Database: accounting_db
-- ============================================

CREATE DATABASE IF NOT EXISTS accounting_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE accounting_db;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 收支分类表
-- ============================================
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    type VARCHAR(10) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    INDEX idx_category_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支分类表';

-- ============================================
-- 账单表
-- ============================================
CREATE TABLE IF NOT EXISTS t_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    type VARCHAR(10) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    description VARCHAR(255) COMMENT '备注',
    bill_date DATE NOT NULL COMMENT '账单日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_bill_user_id (user_id),
    INDEX idx_bill_date (bill_date),
    INDEX idx_bill_category (category_id),
    INDEX idx_bill_type (type),
    INDEX idx_bill_user_date (user_id, bill_date),
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_bill_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- ============================================
-- 周期账单表
-- ============================================
CREATE TABLE IF NOT EXISTS t_recurring_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    type VARCHAR(10) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    description VARCHAR(255) COMMENT '备注',
    cycle_type VARCHAR(20) NOT NULL COMMENT '周期类型: DAILY/WEEKLY/MONTHLY/YEARLY',
    cycle_value INT DEFAULT 1 COMMENT '周期值',
    next_date DATE NOT NULL COMMENT '下次执行日期',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_rb_user_id (user_id),
    INDEX idx_rb_next_date (next_date, is_active),
    CONSTRAINT fk_rb_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_rb_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周期账单表';

-- ============================================
-- 预算表
-- ============================================
CREATE TABLE IF NOT EXISTS t_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category_id BIGINT COMMENT '分类ID(NULL=总预算)',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    budget_amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    spent_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已花费金额',
    is_over_budget TINYINT(1) DEFAULT 0 COMMENT '是否超支',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_budget (user_id, year, month, category_id),
    INDEX idx_budget_user_year_month (user_id, year, month),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';

-- ============================================
-- 初始数据：收支分类
-- ============================================
INSERT INTO t_category (name, type, icon, sort_order) VALUES
-- 支出分类
('餐饮', 'EXPENSE', '🍔', 1),
('购物', 'EXPENSE', '🛒', 2),
('交通', 'EXPENSE', '🚗', 3),
('住房', 'EXPENSE', '🏠', 4),
('娱乐', 'EXPENSE', '🎮', 5),
('医疗', 'EXPENSE', '💊', 6),
('教育', 'EXPENSE', '📚', 7),
('通讯', 'EXPENSE', '📱', 8),
('服饰', 'EXPENSE', '👔', 9),
('运动', 'EXPENSE', '⚽', 10),
('零食', 'EXPENSE', '🍿', 11),
('其他支出', 'EXPENSE', '💸', 99),
-- 收入分类
('工资', 'INCOME', '💰', 1),
('奖金', 'INCOME', '🎁', 2),
('兼职', 'INCOME', '💼', 3),
('投资收益', 'INCOME', '📈', 4),
('退款', 'INCOME', '↩️', 5),
('其他收入', 'INCOME', '📥', 99);
