package com.mindbridge.wishmap.config

import com.mindbridge.wishmap.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증
                    .requestMatchers("/api/v1/auth/**").permitAll()

                    // 공개 조회 API
                    .requestMatchers(HttpMethod.GET, "/api/v1/restaurants").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/{id}/comments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/place-stats").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()

                    // 인프라 / 개발 도구
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/health", "/actuator/health").permitAll()

                    // 관리자
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() } // for H2 console
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = false
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
