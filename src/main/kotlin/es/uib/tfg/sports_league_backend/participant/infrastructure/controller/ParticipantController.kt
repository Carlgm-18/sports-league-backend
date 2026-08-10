package es.uib.tfg.sports_league_backend.participant.infrastructure.controller

import es.uib.tfg.sports_league_backend.availability.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantAvailabilityUseCase
import es.uib.tfg.sports_league_backend.participant.domain.errors.DorsalAlreadyTaken
import es.uib.tfg.sports_league_backend.participant.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.NotInATeam
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantRetrieveError
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantUpdateError
import es.uib.tfg.sports_league_backend.participant.domain.errors.UnauthorizedAction
import es.uib.tfg.sports_league_backend.participant.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.ParticipantUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ParticipantController(
    private val manageParticipantUseCase: ManageParticipantUseCase,
    private val manageParticipantAvailabilityUseCase: ManageParticipantAvailabilityUseCase
) {
    @GetMapping("/leagues/{leagueId}/my-status")
    fun getParticipantLeagueStatus(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<*> =
        when(val result = manageParticipantUseCase.findParticipant(userId, leagueId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())
            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to
                                when (result.error) {
                                    is ParticipantNotFound -> "participant"

                                    is LeagueNotFound -> "league"
                                    is UserNotFound -> "user"
                            }
                        )
                    )
        }

    @PutMapping("/leagues/{leagueId}/my-status/availability")
    fun updateParticipantLeagueAvailability(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: List<Long>
    ): ResponseEntity<*> =
        when(val result = manageParticipantAvailabilityUseCase.updateParticipantAvailability(userId, leagueId, request)) {
            is DomainResult.Success ->
                ResponseEntity.noContent().build<Unit>()

            is DomainResult.Failure ->
                mapError(result.error)
        }

    @GetMapping("/leagues/{leagueId}/my-status/availability")
    fun getParticipantLeagueAvailability(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> =
        when(val result = manageParticipantAvailabilityUseCase.findParticipantAvailability(userId, leagueId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok(result.data.map { it.toDetailsDTO() })

            is DomainResult.Failure ->
                mapError(result.error)
        }



    @PatchMapping("/participants/{participantId}")
    @PreAuthorize("@participantSecurityGuard.canUpdateParticipant(principal, #participantId)")
    fun updateParticipantLeague(
        @PathVariable participantId: Long,
        @Valid @RequestBody updateRequest: ParticipantUpdateRequest
    ): ResponseEntity<*> =
        when(val result = manageParticipantUseCase.updateParticipantById(participantId, updateRequest)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())
            is DomainResult.Failure ->
                mapError(result.error)
        }

    private fun mapError(error: ParticipantUpdateError): ResponseEntity<*> =
        when(error) {
            is ParticipantNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "participant"
                        )
                    )

            is LeagueNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )

            is NotInATeam ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.NOT_IN_A_TEAM
                        )
                    )

            is UnauthorizedAction ->
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                        mapOf(
                            "error" to ErrorCode.UNAUTHORIZED_ERROR
                        )
                    )

            is DorsalAlreadyTaken ->
                ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                        mapOf(
                            "error" to ErrorCode.DORSAL_ALREADY_TAKEN
                        )
                    )

        }

    private fun mapError(error: ParticipantRetrieveError): ResponseEntity<*> =
        when(error) {
            LeagueNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )

            ParticipantNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "participant"
                        )
                    )

            UserNotFound ->
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
