package com.gk.study.config;

import com.gk.study.interceptor.AccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 配置类
 *
 * 添加跨域支持
 *
 * 添加自定义拦截器
 *
 */

@Configuration
public class MyConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//项目中的所有接口都支持跨域
                .allowedOriginPatterns("*") //所有地址都可以访问，也可以配置具体地址
                .allowCredentials(true)
                .allowedMethods("*");//"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 自定义拦截器
        registry.addInterceptor(new AccessInterceptor())
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(    // 但是排除掉以下 Knife4j 和 Swagger 的静态资源
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
