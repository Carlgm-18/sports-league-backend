package es.uib.tfg.sports_league_backend.user

import es.uib.tfg.sportsapi.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService // Inyección del servicio
) {

    @PostMapping("/api/v1/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: UserCreateRequest) {
        userService.registerUser(request)
    }

    @PostMapping("/api/v1/auth/login")
    fun login(@Valid @RequestBody request: UserLoginRequest): UserAuthResponse {
        return userService.login(request)
    }

    @GetMapping("/api/v1/users/me")
    fun getCurrentUser(): UserDetails {
        // El ID del usuario se sacará del token JWT en el futuro
        TODO("Not yet implemented")
    }

    @PatchMapping("/api/v1/users/me")
    fun updateCurrentUser(@Valid @RequestBody request: UserUpdateRequest): UserDetails {
        TODO("Not yet implemented")
    }
}