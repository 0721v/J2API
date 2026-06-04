package com.apiplatform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端页面控制器
 * 用于处理Vue应用的路由请求，实现前后端共用端口
 */
@Controller
public class FrontendController {

    /**
     * 处理所有前端路由请求
     * 将所有非API请求转发到index.html，由Vue Router处理
     */
    @GetMapping(value = {"/", "/login", "/register", "/dashboard", "/chat", "/models", 
                         "/tokens", "/recharge", "/settings", "/report", "/usage", "/docs",
                         "/admin/**", "/agent/**"})
    public String forwardToIndex(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 如果是API请求，不转发
        if (requestUri.startsWith("/api/")) {
            return "forward:/error";
        }
        return "index.html";
    }
}