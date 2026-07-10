# 记账系统 - 后端服务

> 基于 Spring Boot 3.x 的个人记账微服务系统

## 📋 项目简介

将命令行记账软件升级为完整的后端服务系统，支持多用户、权限管理、MySQL 数据库存储，提供规范的 RESTful API 接口。

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| Spring Data JPA | 3.x | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| JJWT | 0.12.3 | JWT 令牌 |
| BCrypt | - | 密码加密 |
| Lombok | - | 代码简化 |

## 🏗️ 项目架构

```
src/main/java/com/accounting/system/
├── AccountingApplication.java      # 启动类
├── config/                          # 配置
│   ├── SecurityConfig.java          # Spring Security 配置
│   └── DataInitializer.java         # 初始数据加载
├── controller/                      # 控制器层 (MVC-C)
│   ├── AuthController.java          # 登录注册
│   ├── BillController.java          # 账单管理
│   ├── UserController.java          # 用户信息
│   ├── BudgetController.java        # 预算管理
│   ├── RecurringBillController.java # 周期账单
│   ├── CategoryController.java      # 分类查询
│   └── AdminController.java         # 管理员功能
├── service/                         # 服务接口层
│   ├── AuthService.java
│   ├── BillService.java
│   ├── UserService.java
│   ├── BudgetService.java
│   ├── RecurringBillService.java
│   └── AdminService.java
├── service/impl/                    # 服务实现层 (MVC-M)
│   ├── AuthServiceImpl.java
│   ├── BillServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── BudgetServiceImpl.java
│   ├── RecurringBillServiceImpl.java
│   └── AdminServiceImpl.java
├── model/
│   ├── entity/                      # 数据实体
│   │   ├── User.java
│   │   ├── Bill.java
│   │   ├── Category.java
│   │   ├── RecurringBill.java
│   │   └── Budget.java
│   ├── dto/                         # 请求对象
│   └── vo/                          # 响应对象
├── repository/                      # 数据访问层
├── security/                        # 安全模块
│   ├── JwtTokenProvider.java        # JWT 生成/解析
│   ├── JwtAuthenticationFilter.java # JWT 过滤器
│   └── UserPrincipal.java           # 用户主体
└── exception/                       # 异常处理
    ├── GlobalExceptionHandler.java
    ├── BusinessException.java
    └── UnauthorizedException.java
```

## 🚀 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 数据库配置

创建 MySQL 数据库并修改 `application.yml` 中的连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/accounting_db?...
    username: root
    password: your_password
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 📡 API 接口一览

### 认证接口（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/account/api/auth/register` | 用户注册 |
| POST | `/account/api/auth/login` | 用户登录 |

### 账单接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/account/api/bills` | 添加账单 |
| GET | `/account/api/bills` | 查询账单列表（分页/筛选） |
| GET | `/account/api/bills/{id}` | 查询账单详情 |
| PUT | `/account/api/bills/{id}` | 修改账单 |
| DELETE | `/account/api/bills/{id}` | 删除账单 |
| GET | `/account/api/bills/balance` | 查询总余额 |
| GET | `/account/api/bills/statistics/category` | 按分类统计 |
| GET | `/account/api/bills/statistics/time` | 按时间范围统计 |

### 用户接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/account/api/users/profile` | 查询个人信息 |
| PUT | `/account/api/users/profile` | 修改个人信息 |
| PUT | `/account/api/users/password` | 修改密码 |

### 预算接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/account/api/budgets` | 设置月度预算 |
| GET | `/account/api/budgets` | 查询预算列表 |
| GET | `/account/api/budgets/usage` | 预算使用情况（含预警） |

### 周期账单接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/account/api/recurring-bills` | 创建周期账单 |
| GET | `/account/api/recurring-bills` | 查询周期账单 |
| DELETE | `/account/api/recurring-bills/{id}` | 删除周期账单 |

### 分类接口（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/account/api/categories` | 获取分类列表 |

### 管理员接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/account/api/admin/users` | 用户列表 |
| PUT | `/account/api/admin/users/{id}/status` | 禁用/启用用户 |
| PUT | `/account/api/admin/users/{id}/reset-password` | 重置密码 |
| GET | `/account/api/admin/statistics` | 系统统计 |

## 🎯 功能清单  

- [x] 用户注册与登录（BCrypt 密码加密）
- [x] JWT 认证与权限拦截
- [x] 账单 CRUD（增删改查）
- [x] 总余额计算（收入 - 支出）
- [x] 按分类统计收支
- [x] 个人信息查询与修改
- [x] 密码修改
- [x] 管理员：用户列表、禁用/启用、重置密码、系统统计
- [x] 分页查询 + 时间范围筛选 + 分类筛选 + 关键词搜索
- [x] 按时间范围统计收支
- [x] 月度预算设置（支持总预算和分类预算）
- [x] 实时预算使用情况（已用/剩余/百分比/超支预警）
- [x] 周期性账单（每天/每周/每月/每年自动生成）

## 📄 API 文档

完整的 OpenAPI 3.0 格式文档见 `api-doc.yaml`，需导入 Apifox 使用。
