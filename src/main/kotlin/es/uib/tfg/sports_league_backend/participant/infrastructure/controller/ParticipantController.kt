package es.uib.tfg.sports_league_backend.participant.infrastructure.controller;

import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/participants")
class ParticipantController(
    private val participantService: ParticipantService
) {
    @PatchMapping("/participants/{participantId}")
    fun updateParticipantLeague(
        @PathVariable participantId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody updateRequest: ParticipantUpdateRequest
    ): ResponseEntity<*> =
        when(val result = participantService.updateParticipantById(participantId, userId, updateRequest)) {
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


}
