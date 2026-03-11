package com.novel2video.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 
 * @param <T> 响应数据类型
 * @author developer
 * @since 2026-03-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 状态码 */
    private Integer code;
    
    /** 消息 */
    private String message;
    
    /** 数据 */
    private T data;
    
    /** 时间戳 */
    private Long timestamp;
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.FAILURE.getCode(), message, null);
    }
    
    /**
     * 失败响应（带错误码）
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
    
    /**
     * 失败响应（带错误码和消息）
     */
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }
    
    /**
     * 失败响应（带错误码和数据）
     */
    public static <T> Result<T> error(ErrorCode errorCode, T data) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), data);
    }
    
    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code.equals(ErrorCode.SUCCESS.getCode());
    }
}
