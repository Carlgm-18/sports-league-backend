package es.uib.tfg.sports_league_backend.incidence.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.incidence.application.IncidenceService
import es.uib.tfg.sports_league_backend.incidence.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.incidence.infrastructure.mapper.toDTO
import es.uib.tfg.sportsapi.dto.IncidenceCreateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/leagues/{leagueId}/incidences")
class IncidenceController(
    private val incidenceService: IncidenceService
) {
    @PostMapping
    fun createIncidence(
        @PathVariable leagueId: Long,
        @Valid @RequestBody incidence: IncidenceCreateRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> =
        when(val result = incidenceService.save(userId, leagueId, incidence)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDTO())

            is DomainResult.Failure ->
                when (result.error) {
                    ParticipantNotFound ->
                        ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.UNAUTHORIZED_ERROR
                                )
                            )
                }
        }

}