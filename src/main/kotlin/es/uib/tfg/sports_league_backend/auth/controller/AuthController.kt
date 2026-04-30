package es.uib.tfg.sports_league_backend.auth.controller

import es.uib.tfg.sports_league_backend.common.security.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val jwtService: JwtService,
) {

    @PostMapping("/api/v1/auth/token/refresh")
    fun refreshToken(@RequestBody refreshToken: String): ResponseEntity<*> {
        TODO("Not implemented yet")
    }

}