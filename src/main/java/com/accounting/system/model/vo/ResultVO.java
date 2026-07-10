package com.accounting.system.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResultVO<T> success(T data) {
        return ResultVO.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ResultVO<T> success() {
        return ResultVO.<T>builder()
                .code(200)
                .message("success")
                .build();
    }

    public static <T> ResultVO<T> error(int code, String message) {
        return ResultVO.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ResultVO<T> error(String message) {
        return ResultVO.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}
