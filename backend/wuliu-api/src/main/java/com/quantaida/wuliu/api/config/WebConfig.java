package com.quantaida.wuliu.api.config;

import com.quantaida.wuliu.service.system.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 跨域（开发环境）与全量写操作审计埋点。 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuditService auditService;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        Object handler, Exception ex) {
                if (!"GET".equalsIgnoreCase(request.getMethod()) && response.getStatus() < 400) {
                    String path = request.getRequestURI();
                    if (path.startsWith("/api/auth/login") || path.startsWith("/api/driver/login")
                            || path.startsWith("/api/carrier/login")) return;
                    auditService.log(request.getMethod() + " " + path, String.valueOf(response.getStatus()), "");
                }
            }
        }).addPathPatterns("/api/**");
    }
}
