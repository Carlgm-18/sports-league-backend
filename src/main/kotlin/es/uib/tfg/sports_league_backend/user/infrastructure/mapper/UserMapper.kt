package es.uib.tfg.sports_league_backend.user.infrastructure.mapper

import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.application.update.UserUpdateCommand
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.UserAuthResponse
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserCreateResponse
import es.uib.tfg.sportsapi.dto.UserDetails
import es.uib.tfg.sportsapi.dto.UserLoginRequest
import es.uib.tfg.sportsapi.dto.UserSummary
import es.uib.tfg.sportsapi.dto.UserUpdateRequest
import java.net.URI
import java.time.LocalDateTime

// users/me
fun User.toDetailsDTO() =
    UserDetails(
        id!!,
        email,
        "$firstName $lastName",
        category,
        createdAt,
        URI(profileImageUrl ?: ""),
        licenses.map { it.toDTO() }.toList(),
        signature,
    )

// users/register
fun UserCreateRequest.toCommand() =
    UserRegisterCommand(
        email = email,
        firstName = firstName,
        lastName = lastName,
        category = category,
        createdAt = LocalDateTime.now(),
        licenses = licenses,
        password = password,
        confirmPassword = confirmPassword,
    )

fun UserRegisterCommand.toEntity(securePassword: SecurePassword) =
    User(
        id,
        firstName,
        lastName,
        email,
        securePassword,
        category,
        createdAt,
        profileImageUrl,
        signature = signature,
    )

fun UserUpdateRequest.toCommand() =
    UserUpdateCommand(
        firstName,
        lastName,
        licenses
    )

fun User.toCreateResponse() =
    UserCreateResponse(
        firstName,
        lastName,
        email,
        category,
        licenses.map { it.toDTO() }.toList()
    )

fun User.toSummary() =
    UserSummary(
        id!!,
        "$firstName $lastName",
        category,
        URI(profileImageUrl ?: ""),
    )


fun UserLoginRequest.toCommand() =
    UserLoginCommand(
        email,
        password,
    )

fun LoginSessionInfo.toLoginResponse() =
    UserAuthResponse(
        accessToken,
        refreshToken,
        expiresIn.toInt(),
        tokenType,
        user.toSummary(),
    )