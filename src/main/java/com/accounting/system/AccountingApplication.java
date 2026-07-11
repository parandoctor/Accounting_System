package com.accounting.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================
 * 个人记账系统 - 应用启动入口
 * ============================================================
 *
 * 【项目架构概述】
 * 本项目采用经典的分层架构（Layered Architecture），自上而下分为：
 *
 *   ┌──────────────────────────────────────────────┐
 *   │  Controller 层  ── 接收HTTP请求，参数校验      │
 *   │       ↓ 调用                                  │
 *   │  Service 层     ── 业务逻辑处理，事务管理      │
 *   │       ↓ 调用                                  │
 *   │  Repository 层  ── 数据访问，CRUD操作          │
 *   │       ↓ 映射                                  │
 *   │  Entity 层      ── 数据库表映射（ORM）         │
 *   └──────────────────────────────────────────────┘
 *
 *  横向支撑组件：
 *  - Security 层：JWT认证 + Spring Security权限控制
 *  - Exception 层：全局异常拦截，统一错误响应
 *  - Config 层：Spring Bean配置，启动时数据初始化
 *  - DTO/VO 层：数据传输对象，隔离内外表示
 *
 * 【核心注解说明】
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 *   标记此类为SpringBoot主配置类，自动扫描同级及子包下的所有组件
 * @EnableScheduling 启用Spring的定时任务调度，用于周期性账单的自动生成
 *
 * @author 小爪
 */
@SpringBootApplication
@EnableScheduling
public class AccountingApplication {

    /**
     * 应用入口：启动内嵌Tomcat，初始化Spring IoC容器，完成自动配置
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountingApplication.class, args);
    }
}
