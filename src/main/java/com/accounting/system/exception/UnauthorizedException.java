package com.accounting.system.exception;

/**
 * ============================================================
 * 未授权异常 —— 用户未登录或Token无效时抛出
 * ============================================================
 *
 * 【与BusinessException的区别】
 *  BusinessException     → 400 Bad Request（业务规则违反，如"用户名已存在"）
 *  UnauthorizedException → 401 Unauthorized（认证失败，需要登录）
 *
 * 【使用场景】
 * - Token过期或无效
 * - 未登录访问受保护接口
 * - 登录凭证不合法
 *
 * 【与Spring Security认证异常的关系】
 * Spring Security的 AuthenticationException 会在过滤器链中被捕获，
 * 但某些场景（如手动校验Token）可能需要主动抛出本异常，
 * 由 GlobalExceptionHandler 统一转为401响应
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
