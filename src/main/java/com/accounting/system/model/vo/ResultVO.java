package com.accounting.system.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 * 统一响应体 —— 前端与后端的数据契约
 * ============================================================
 *
 * 【设计理念】
 * 所有API接口返回统一格式，前端只需处理一种响应结构：
 *
 *   成功响应：
 *   { "code": 200, "message": "success", "data": {...} }
 *
 *   失败响应：
 *   { "code": 400, "message": "用户名已存在", "data": null }
 *
 * 【泛型设计】
 * <T> 使得data字段可以承载任意类型：ResultVO<UserVO>、ResultVO<List<BillVO>>等
 * 
 * 【@JsonInclude(NON_NULL)】
 * 当data为null时，JSON序列化会省略data字段（而非输出 "data": null）
 * 减少响应体积，前端也更易处理
 *
 * 【工厂方法模式】
 * 提供静态工厂方法统一创建成功/失败响应，避免在Controller中重复写Builder代码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {
    /** 状态码：200成功，400业务错误，401未认证，403无权限，500服务器错误 */
    private int code;
    /** 提示消息：成功时为"success"，失败时为具体错误原因 */
    private String message;
    /** 响应数据：泛型，可为任意类型；null时JSON中不出现此字段 */
    private T data;

    /** 成功响应（带数据） */
    public static <T> ResultVO<T> success(T data) {
        return ResultVO.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /** 成功响应（无数据，如删除操作） */
    public static <T> ResultVO<T> success() {
        return ResultVO.<T>builder()
                .code(200)
                .message("success")
                .build();
    }

    /** 错误响应（自定义错误码） */
    public static <T> ResultVO<T> error(int code, String message) {
        return ResultVO.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /** 错误响应（默认500错误码） */
    public static <T> ResultVO<T> error(String message) {
        return ResultVO.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}
