package com.accounting.system.exception;

import lombok.Getter;

/**
 * ============================================================
 * 业务异常 —— 将业务规则违反转化为可捕获的异常
 * ============================================================
 *
 * 【设计意图】
 * 传统做法是在Service层返回错误码，调用方需要逐层判断：
 *   if (result == ERROR) return errorResponse;
 * 使用业务异常后，Service层直接抛出，由全局异常处理器统一捕获：
 *   throw new BusinessException("用户名已存在");
 * 优点：代码简洁、错误处理集中、不会遗漏
 *
 * 【与 GlobalExceptionHandler 的协作】
 * BusinessException 被抛出 → Controller层不处理 → 被 @RestControllerAdvice 拦截
 * → handleBusinessException() 方法处理 → 返回 HTTP 400 + 统一错误格式
 *
 * 【使用示例】
 *   // 简单使用（默认400状态码）
 *   throw new BusinessException("分类不存在");
 *
 *   // 自定义错误码
 *   throw new BusinessException(1001, "余额不足");
 */
@Getter // 需要getCode()让异常处理器读取
public class BusinessException extends RuntimeException {
    /** 业务错误码，用于前端区分不同类型的错误 */
    private final int code;

    /**
     * 自定义错误码构造
     * @param code    业务错误码
     * @param message 错误描述（会直接返回给前端）
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 默认400错误码构造（最常用）
     * @param message 错误描述
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
