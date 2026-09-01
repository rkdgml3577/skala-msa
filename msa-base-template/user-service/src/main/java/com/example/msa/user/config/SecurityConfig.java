package com.example.msa.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

/**
 * 개발용 기본 보안 설정.
 *
 * <p>기본값은 모든 요청을 허용(permitAll)해서 인프라(Auth Server/Gateway) 없이도
 * 서비스를 단독 실행할 수 있게 한다. JWT 검증을 켜려면 아래 authorizeHttpRequests /
 * oauth2ResourceServer 주석을 해제하고 application.yml 의 issuer-uri / jwk-set-uri 를 설정한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                return config;
            }))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 개발 편의: 전부 허용. 운영에서는 아래 프로덕션 설정으로 교체.
                .anyRequest().permitAll()
            );

        // === 프로덕션 예시 (JWT 검증 활성화) ===
        // http.authorizeHttpRequests(auth -> auth
        //         .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        //         .requestMatchers("/api/users/internal/**").hasAuthority("SCOPE_service.read")
        //         .anyRequest().authenticated()
        //     )
        //     .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }
}
