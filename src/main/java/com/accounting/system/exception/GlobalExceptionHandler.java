package com.accounting.system.exception;

import com.accounting.system.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ============================================================
 * 全局异常处理器 —— Spring AOP 实现的统一异常拦截
 * ============================================================
 *
 * 【核心理念：异常即响应】
 * 传统的异常处理方式：每个Controller方法写 try-catch，分散且容易遗漏。
 * 本类的处理方式：利用Spring AOP切面，拦截所有Controller抛出的异常，
 * 统一转换为 ResultVO 格式的HTTP响应。Service层只需专注业务逻辑，
 * 遇到问题直接 throw，无需关心HTTP细节。
 *
 * 【执行流程】
 *
 *   Controller → Service → throw new BusinessException("xxx")
 *       ↑                                    │
 *       │ 正常返回                           │ 异常抛出
 *       │                                    ▼
 *   ┌─────────────────────────────────────────────────┐
 *   │  @RestControllerAdvice 横切拦截                  │
 *   │  根据 @ExceptionHandler 注解匹配异常类型          │
 *   │  将异常转换为 ResponseEntity<ResultVO>           │
 *   └─────────────────────────────────────────────────┘
 *       │
 *       ▼
 *   客户端收到：{ "code": 400, "message": "xxx", "data": null }
 *
 * 【异常分类与HTTP状态码映射】
 *   BusinessException              → 400 BAD_REQUEST    （业务逻辑错误）
 *   UnauthorizedException          → 401 UNAUTHORIZED   （未认证）
 *   AuthenticationException        → 401 UNAUTHORIZED   （Spring Security认证失败）
 *   AccessDeniedException          → 403 FORBIDDEN      （权限不足）
 *   MethodArgumentNotValidException → 400 BAD_REQUEST   （参数校验失败）
 *   Exception (兜底)               → 500 INTERNAL_ERROR （未知错误）
 */
@Slf4j                  // 日志输出
@RestControllerAdvice   // = @ControllerAdvice + @ResponseBody，拦截所有Controller异常
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 —— 最常见的异常类型
     * 
     * Service层几乎所有主动抛出的异常都是 BusinessException：
     *   throw new BusinessException("用户名已存在");
     *   throw new BusinessException("分类不存在");
     * 
     * 返回400状态码 + 统一错误格式，前端直接取 message 展示给用户
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResultVO<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultVO.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理未授权异常 —— 用户未登录或Token无效
     * 返回401，前端收到后应跳转登录页
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResultVO<Void>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultVO.error(401, e.getMessage()));
    }

    /**
     * 处理Spring Security认证异常 —— 登录失败
     * 
     * 与UnauthorizedException的区别：
     * - UnauthorizedException 是自定义的，在业务代码中手动抛出
     * - AuthenticationException 是Spring Security框架抛出的，如密码错误、用户不存在
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResultVO<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultVO.error(401, "认证失败，请检查用户名和密码"));
    }

    /**
     * 处理权限拒绝异常 —— 已登录但无权访问（如普通用户访问管理接口）
     * 
     * 触发条件：
     * - 普通用户访问 /api/admin/** 接口
     * - @PreAuthorize 注解校验失败
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultVO<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultVO.error(403, "权限不足"));
    }

    /**
     * 处理参数校验异常 —— @Valid 注解校验失败
     * 
     * 触发场景：DTO中的 @NotBlank、@Size、@DecimalMin 等校验不通过
     * 例如：注册时用户名长度不足3位、账单金额为负数等
     * 
     * 此处将多个字段的校验错误合并为一条消息：
     *   原始：{"username":"长度3-50位","password":"长度6-100位"}
     *   合并后："长度3-50位, 长度6-100位"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultVO<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultVO.error(400, message));
    }

    /**
     * 兜底异常处理 —— 捕获所有未被上述方法处理的异常
     * 
     * 这是最后的安全网，防止敏感异常信息（如堆栈跟踪）泄露给客户端。
     * 服务端会记录完整堆栈（log.error），但客户端只收到通用的"服务器内部错误"。
     * 
     * 安全考虑：生产环境绝不应该把 e.getMessage() 直接返回给前端，
     * 可能泄露数据库结构、SQL语句等敏感信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultVO<Void>> handleException(Exception e) {
        log.error("Unexpected error: ", e);  // 输出完整堆栈，便于排查
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultVO.error(500, "服务器内部错误"));
    }
}
