package com.apiplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author API Platform Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 不需要 CSRF）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的路径（注意：context-path 为 /api，所以这里不需要加 /api 前缀）
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/oauth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/druid/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                // Swagger UI 路径
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                // 前端 SPA 路径（Vue Router history 模式）
                .requestMatchers("/").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/dashboard").permitAll()
                .requestMatchers("/chat").permitAll()
                .requestMatchers("/models").permitAll()
                .requestMatchers("/tokens").permitAll()
                .requestMatchers("/settings").permitAll()
                .requestMatchers("/recharge").permitAll()
                .requestMatchers("/report").permitAll()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/agent/**").permitAll()
                .requestMatchers("/docs").permitAll()
                // 用户端模型接口允许匿名访问
                .requestMatchers("/models/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 禁用表单登录（使用 JWT）
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 配置无状态会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
