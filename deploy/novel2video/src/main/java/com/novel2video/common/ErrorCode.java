package com.novel2video.common;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author developer
 * @since 2026-03-10
 */
@Getter
public enum ErrorCode {
    
    // 通用错误码 (1000-1999)
    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    
    // 参数错误 (2000-2999)
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "缺少必要参数"),
    PARAM_INVALID(2003, "参数无效"),
    
    // 业务错误 (3000-3999)
    BUSINESS_ERROR(3001, "业务异常"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_EXISTS(3003, "数据已存在"),
    DATA_CONFLICT(3004, "数据冲突"),
    
    // 认证授权 (4000-4999)
    UNAUTHORIZED(4001, "未授权"),
    FORBIDDEN(4003, "禁止访问"),
    TOKEN_EXPIRED(4005, "Token 已过期"),
    
    // 系统错误 (5000-5999)
    SYSTEM_ERROR(5000, "系统异常"),
    SERVICE_UNAVAILABLE(5003, "服务不可用"),
    
    // 文件相关 (6000-6999)
    FILE_NOT_FOUND(6001, "文件不存在"),
    FILE_UPLOAD_FAILED(6002, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(6003, "文件下载失败"),
    
    // AI 服务相关 (7000-7999)
    AI_SERVICE_ERROR(7001, "AI 服务异常"),
    AI_SERVICE_TIMEOUT(7002, "AI 服务超时"),
    AI_SERVICE_RATE_LIMIT(7003, "AI 服务请求超限"),
    
    // 视频生成相关 (8000-8999)
    VIDEO_GENERATE_FAILED(8001, "视频生成失败"),
    VIDEO_GENERATE_TIMEOUT(8002, "视频生成超时"),
    VIDEO_TASK_NOT_FOUND(8003, "视频任务不存在");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
