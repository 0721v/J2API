package com.apiplatform.config;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 用户ID参数解析器
 * 将 @RequestAttribute("userId") 注解的参数自动解析为当前登录用户的ID
 */
@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 支持 Long 类型的 userId 参数
        return "userId".equals(parameter.getParameterName()) 
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // 从认证信息中获取用户ID（我们在JwtAuthenticationFilter中设置的是userId作为principal）
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return principal;
        }
        
        return null;
    }
}
