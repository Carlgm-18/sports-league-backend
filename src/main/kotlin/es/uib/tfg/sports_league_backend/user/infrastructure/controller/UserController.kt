package es.uib.tfg.sports_league_backend.user.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.user.application.UserService
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRegisterError
import es.uib.tfg.sports_league_backend.user.domain.errors.UserRetrieveError
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toCommand
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toCreateResponse
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.user.infrastructure.mapper.toLoginResponse
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserDetails
import es.uib.tfg.sportsapi.dto.UserLoginRequest
import es.uib.tfg.sportsapi.dto.UserUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
                            .body(mapOf("error" to ErrorCode.EMAIL_ALREADY_EXISTS))

                    UserRegisterError.PasswordEncodingFailed ->
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(mapOf("error" to ErrorCode.PASSWORD_ENCODING_FAILED))

                    UserRegisterError.PasswordsDontMatch ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to ErrorCode.PASSWORDS_DONT_MATCH))
                }
        }

    @PostMapping("/api/v1/users/login")
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<*> =
        when (val result = userService.login(request.toCommand())) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toLoginResponse())

            is DomainResult.Failure ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to ErrorCode.INVALID_CREDENTIALS))
        }

    @GetMapping("/api/v1/users/me")
    fun getCurrentUser(@AuthenticationPrincipal principal: Long): ResponseEntity<*> =
        when (val user = userService.findUserById(principal)) {
            is DomainResult.Success -> {
                ResponseEntity.ok(user.data.toDetailsDTO())
            }

            is DomainResult.Failure -> {
                when (user.error) {
                    is UserRetrieveError.UserNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "user"
                                )
                            )
                    }
                }
            }
        }

    @PatchMapping("/api/v1/users/me")
    fun updateCurrentUser(
        @AuthenticationPrincipal principal: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<*> {
        TODO("Not yet implemented")
    }
}