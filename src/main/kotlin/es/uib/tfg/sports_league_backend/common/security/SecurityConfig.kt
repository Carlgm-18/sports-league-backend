package es.uib.tfg.sports_league_backend.common.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(/*private val jwtAuthFilter: JwtAuthenticationFilter*/) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                // DEBUG: Permitir todas las peticiones temporalmente
                auth.anyRequest().permitAll()
            }
            // La gestión de sesión y las reglas específicas se ignoran por ahora
            // .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // .authorizeHttpRequests { auth ->
            //     auth.requestMatchers("/api/v1/auth/**").permitAll()
            //     auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
            //     auth.requestMatchers(HttpMethod.GET, "/api/v1/leagues/**").permitAll()
            //     auth.requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
            //     auth.requestMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
            //     auth.anyRequest().authenticated()
            // }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.allowCredentials = true
        config.allowedOriginPatterns = listOf("*") // En prod lo cambiarás por "https://tuweb.com"
        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")

        source.registerCorsConfiguration("/**", config)
        return source
    }
}