package com.persona.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.persona.security.AuthInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final RateLimitInterceptor rateLimitInterceptor;
    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor, AuthInterceptor authInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/config/public",
                        "/api/health"
                );
    }
}
