package com.apiplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置静态资源和 SPA fallback
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源处理 - 从 classpath:/static/ 目录
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000); // 1年缓存

        // 前端资源处理
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(31536000);

        // 网站图标
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA fallback - 所有未匹配的路径都返回 index.html
        // 这样 Vue Router 的 history 模式才能正常工作
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
        
        // 根路径
        registry.addViewController("/")
                .setViewName("forward:/index.html");
    }
}
