package com.quantaida.wuliu.api.config;

import com.quantaida.wuliu.common.result.R;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 统一包装：业务接口返回对象自动包成 {code,message,data}；字符串（打印页）与 R 不重复包装。 */
@RestControllerAdvice(basePackages = "com.quantaida.wuliu.api.controller")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof R || body instanceof String || body instanceof byte[]) return body;
        if (selectedContentType != null && selectedContentType.includes(MediaType.TEXT_HTML)) return body;
        return R.ok(body);
    }
}
