package es.uib.tfg.sports_league_backend.sign.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.sign.application.SignService
import es.uib.tfg.sports_league_backend.sign.domain.errors.SignNotFound
import es.uib.tfg.sports_league_backend.sign.infrastructure.mapper.toDTO
import es.uib.tfg.sportsapi.dto.SignImageUrl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/me/signature")
class SignController(
    val signService: SignService
) {

    @PostMapping
    fun createUserSign(
        @Valid @RequestBody request: SignImageUrl,
        @AuthenticationPrincipal principal: Long
    ): ResponseEntity<*> =
        TODO("Not implemented yet")

    @GetMapping
    fun getUserSign(@AuthenticationPrincipal principal: Long): ResponseEntity<*> =
        when (val result = signService.getSignByUserId(principal)) {
            is DomainResult.Failure ->
                when(result.error) {
                    is SignNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "sign",
                                )
                            )
                }
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())
        }
}