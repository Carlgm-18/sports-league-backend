package es.uib.tfg.sports_league_backend.user.infrastructure.controller

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.domain.errors.UserLoginError
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRegisterError
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRetrieveError
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toCommand
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toCreateResponse
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toLoginResponse
import es.uib.tfg.sportsapi.dto.UserAuthResponse
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserDetails
import es.uib.tfg.sportsapi.dto.UserLoginRequest
import es.uib.tfg.sportsapi.dto.UserUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {

    @PostMapping("/api/v1/users/register")
    fun register(@RequestBody @Valid request: UserCreateRequest): ResponseEntity<*> =
        when (
            val result = userService.registerUser(request.toCommand())
        ) {

            is DomainResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toCreateResponse())

            is DomainResult.Failure ->
                when (result.error) {
                    UserRegisterError.EmailAlreadyExists ->
                        ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(mapOf("error" to "El email ${request.email} ya está registrado"))

                    UserRegisterError.PasswordEncodingFailed ->
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(mapOf("error" to "Error procesando la seguridad de la cuenta"))

                    UserRegisterError.PasswordsDontMatch ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to "Las contraseñas no coinciden"))
            }
        }

    @PostMapping("/api/v1/users/login")
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<*> =
        when (val result = userService.login(request.toCommand())) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toLoginResponse())

            is DomainResult.Failure ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Credenciales inválidas"))
        }

    data class UserId(val id: Int)

    @GetMapping("/api/v1/users/me")
    fun getCurrentUser(@RequestBody request: UserId): ResponseEntity<*> {
        // El ID del usuario se sacará del token JWT en el futuro
        return when (val user = userService.getUserById(request.id)) {
            is DomainResult.Success -> {
                ResponseEntity
                    .status(HttpStatus.FOUND)
                    .body(user.data.toDetailsDTO())
            }

            is DomainResult.Failure -> {
                when (user.error) {
                    is UserRetrieveError.UserNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(user.error)
                    }
                }
            }
        }
    }

    @PatchMapping("/api/v1/users/me")
    fun updateCurrentUser(@Valid @RequestBody request: UserUpdateRequest): UserDetails {
        TODO("Not yet implemented")
    }
}