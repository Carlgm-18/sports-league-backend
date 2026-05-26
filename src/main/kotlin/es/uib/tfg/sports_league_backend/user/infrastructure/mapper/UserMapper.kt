package es.uib.tfg.sports_league_backend.user.infrastructure.mapper

import es.uib.tfg.sports_league_backend.user.application.login.LoginSessionInfo
import es.uib.tfg.sports_league_backend.user.application.login.UserLoginCommand
import es.uib.tfg.sports_league_backend.user.application.register.UserRegisterCommand
import es.uib.tfg.sports_league_backend.user.domain.SecurePassword
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.UserAuthResponse
import es.uib.tfg.sportsapi.dto.UserCreateRequest
import es.uib.tfg.sportsapi.dto.UserCreateResponse
import es.uib.tfg.sportsapi.dto.UserDetails
import es.uib.tfg.sportsapi.dto.UserLoginRequest
import es.uib.tfg.sportsapi.dto.UserSummary
import java.net.URI
import java.time.LocalDateTime

// users/me
fun User.toDetailsDTO() =
    UserDetails(
        userId = this.id!!,
        email = this.email,
        fullName = "${this.firstName} ${this.lastName}",
        category = this.category,
        profileImageUrl = URI(this.profileImageUrl ?: ""),
        licenses = this.licenses ?: listOf(),
        signature = this.signature,
        createdAt = this.createdAt
    )

// users/register
fun UserCreateRequest.toCommand() =
    UserRegisterCommand(
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        category = this.category,
        createdAt = LocalDateTime.now(),
        licenses = this.licenses,
        password = this.password,
        confirmPassword = this.confirmPassword,
        profileImageUrl = "",
        signature = null
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
        licenses,
        signature,
    )

fun User.toCreateResponse() =
    UserCreateResponse(
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        licenses = this.licenses,
        category = this.category,
    )

fun User.toSummary() =
    UserSummary(
        id!!,
        "$firstName $lastName",
        category,
        URI(profileImageUrl ?: ""),
    )

// users/login endpoint

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