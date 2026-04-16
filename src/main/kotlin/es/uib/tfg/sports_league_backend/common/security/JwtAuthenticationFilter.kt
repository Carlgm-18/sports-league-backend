package es.uib.tfg.sports_league_backend.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)

        try {
            val userEmail = jwtService.extractEmail(jwt)

            if (SecurityContextHolder.getContext().authentication == null) {
                if (jwtService.isTokenValid(jwt)) {
                    // Si el token es válido, le decimos a Spring Security que este usuario está autenticado
                    val authToken = UsernamePasswordAuthenticationToken(userEmail, null, emptyList())
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {
            // Token inválido, expirado o malformado
        }

        filterChain.doFilter(request, response)
    }
}