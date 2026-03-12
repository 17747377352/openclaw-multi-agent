package com.novel2video.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 兼容层：将历史 Map 响应补齐为前端可识别的统一结构。
 */
@RestControllerAdvice
public class ApiResponseCompatAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof Map)) {
            return body;
        }

        Map<String, Object> source = (Map<String, Object>) body;
        Map<String, Object> normalized = new LinkedHashMap<>(source);

        Object success = normalized.get("success");
        Object code = normalized.get("code");

        if (code == null && success instanceof Boolean) {
            normalized.put("code", (Boolean) success ? 200 : 500);
        } else if (success == null && code instanceof Number) {
            normalized.put("success", ((Number) code).intValue() == 200);
        }

        if (!normalized.containsKey("message")) {
            normalized.put("message", Boolean.TRUE.equals(normalized.get("success")) ? "操作成功" : "操作失败");
        }

        if (!normalized.containsKey("data")) {
            if (normalized.containsKey("imageUrl")) {
                normalized.put("data", normalized.get("imageUrl"));
            } else if (normalized.containsKey("projectId")) {
                normalized.put("data", normalized.get("projectId"));
            } else if (normalized.containsKey("taskId")) {
                normalized.put("data", normalized.get("taskId"));
            }
        }

        return normalized;
    }
}
