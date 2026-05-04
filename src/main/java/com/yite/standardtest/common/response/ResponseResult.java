package com.yite.standardtest.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.StandardException;

/**
 * 在 Controller 层返回 ResponseResult 即可
 */
@Data   //Jackson 负责：把 Java 对象“序列化”为 JSON
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {

    private int code;       // 状态码，比如200、500
    private String message; // 提示信息
    private T data;         // 返回的数据

    // 成功返回
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "成功", data);
    }

    public static <T> ResponseResult<T> success(T data, String msg) {
        return new ResponseResult<>(200, msg, data);
    }

    // 成功返回，只传 message，没有 data
    public static <T> ResponseResult<T> successMessage(String msg) {
        return new ResponseResult<>(200, msg, null);
    }

    // 失败返回
    public static <T> ResponseResult<T> error(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    public static <T> ResponseResult<T> error(String msg) {
        return new ResponseResult<>(500, msg, null);
    }

    
}
