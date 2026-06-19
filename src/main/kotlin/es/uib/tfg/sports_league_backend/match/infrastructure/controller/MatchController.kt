package es.uib.tfg.sports_league_backend.match.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.match.application.MatchService
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.match.domain.errors.AssignRefereeError
import es.uib.tfg.sports_league_backend.match.domain.errors.IncompatibleMatchAndReferee
import es.uib.tfg.sports_league_backend.match.domain.errors.InexistentAvailabilityForThisRound
import es.uib.tfg.sports_league_backend.match.domain.errors.InvalidState
import es.uib.tfg.sports_league_backend.match.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchAlreadyEnded
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchNotScheduledYet
import es.uib.tfg.sports_league_backend.match.domain.errors.MatchUpdateError
import es.uib.tfg.sports_league_backend.match.domain.errors.NoAvailableReferees
import es.uib.tfg.sports_league_backend.match.domain.errors.NotRefereeParticipant
import es.uib.tfg.sports_league_backend.match.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalAlreadyResolved
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalCreateError
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.ProposalResolveError
import es.uib.tfg.sports_league_backend.match.domain.errors.RefereeNotAssigned
import es.uib.tfg.sports_league_backend.match.domain.errors.ResolveOwnProposalError
import es.uib.tfg.sports_league_backend.match.domain.errors.ScheduleAlreadyTaken
import es.uib.tfg.sports_league_backend.match.domain.errors.TeamNotFound
import es.uib.tfg.sports_league_backend.match.domain.errors.TeamNotInMatch
import es.uib.tfg.sports_league_backend.match.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.match.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.MatchDateProposalCreateRequest
import es.uib.tfg.sportsapi.dto.MatchDateProposalResolveRequest
import es.uib.tfg.sportsapi.dto.MatchUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/v1/matches/{matchId}")
class MatchController(
    private val matchService: MatchService
) {

    @GetMapping
    fun getMatchDetails(@PathVariable matchId: Long): ResponseEntity<*> =
        when(val result = matchService.finMatchById(matchId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok(
                        result.data
                            .toDetailsDTO(matchService.getActiveProposal(matchId))
                    )

            is DomainResult.Failure ->
                when (result.error) {
                    MatchNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "match"
                                )
                            )
                }
        }

    @PatchMapping
    fun updateMactch(
        @PathVariable matchId: Long,
        @Valid @RequestBody request: MatchUpdateRequest
    ): ResponseEntity<*> =
        when(val result = matchService.updateMatch(matchId, request)) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok(result.data.toDetailsDTO(null))

            is DomainResult.Failure ->
                mapError(result.error)
        }

    private fun mapError(error: MatchUpdateError): ResponseEntity<*> =
        when(error) {
            InvalidState ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.INVALID_MATCH_STATE
                        )
                    )
            MatchAlreadyEnded ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.MATCH_ENDED
                        )
                    )
            MatchNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "match"
                        )
                    )
            RefereeNotAssigned ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.REFEREE_NOT_ASSIGNED
                        )
                    )
            TeamNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "team"
                        )
                    )
            TeamNotInMatch ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.WINNER_NOT_IN_MATCH
                        )
                    )
        }


    @PostMapping("/schedule/proposals")
    fun createProposal(
        @PathVariable matchId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: MatchDateProposalCreateRequest
    ): ResponseEntity<*> =
        when (val result = matchService.createProposal(matchId, userId, request.dateTimeSlotId)) {
            is DomainResult.Success -> ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Proposal submitted successfully"))
            is DomainResult.Failure -> {
                mapError(result.error)
            }
        }


    @PatchMapping("/schedule/proposals/{proposalId}")
    fun resolveProposal(
        @PathVariable matchId: Long,
        @PathVariable proposalId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: MatchDateProposalResolveRequest
    ): ResponseEntity<*> {
        return when (val result = matchService.resolveProposal(proposalId, userId, request)) {
            is DomainResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toDetailsDTO())
            is DomainResult.Failure -> {
                mapError(result.error)
            }
        }
    }

    @PostMapping("/referee/auto")
    fun autoAssignReferee(
        @PathVariable matchId: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> {
        return when (val result = matchService.autoAssignReferee(matchId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok(
                        result.data
                                .toDetailsDTO(matchService.getActiveProposal(matchId))
                    )
            is DomainResult.Failure -> {
                mapError(result.error)
            }
        }
    }

    @PutMapping("/referee")
    fun forceAssignReferee(
        @PathVariable matchId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: ForceRefereeRequest,
        @RequestParam refereeType: RefereeType
    ): ResponseEntity<*> {
        return when (
            val result =
                matchService
                    .forceAssignReferee(
                        matchId,
                        request.refereeId,
                        refereeType
                    )
        ) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok(
                        result.data
                                .toDetailsDTO(matchService.getActiveProposal(matchId))
                    )
            is DomainResult.Failure -> {
                mapError(result.error)
            }
        }
    }

    private fun mapError(error: AssignRefereeError): ResponseEntity<*> =
        when (error) {
            MatchNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "match"
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

            IncompatibleMatchAndReferee ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.SUBRESOURCE_MISMATCH,
                            "subresource" to "league",
                            "resources" to listOf("match", "referee")
                        )
                    )
            MatchNotScheduledYet ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.MATCH_NOT_SCHEDULED
                        )
                    )
            NoAvailableReferees ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        mapOf(
                            "error" to ErrorCode.REFEREES_UNAVAILABLE
                        )
                    )
            NotRefereeParticipant ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.PARTICIPANT_NOT_VALID,
                            "roleNeeded" to "referee"
                        )
                    )
        }
    private fun mapError(error: ProposalCreateError): ResponseEntity<*> =
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
            MatchNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "match"
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
            InexistentAvailabilityForThisRound ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        mapOf(
                            "error" to ErrorCode.NO_AVAILABILITY
                        )
                    )

            ScheduleAlreadyTaken ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.SCHEDULE_ALREADY_ASSIGNED
                        )
                    )

    }
    private fun mapError(error: ProposalResolveError): ResponseEntity<*> =
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
            ProposalNotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "proposal"
                        )
                    )
            ProposalAlreadyResolved ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.PROPOSAL_ALREADY_RESOLVED
                        )
                    )
            ResolveOwnProposalError ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOLVE_OWN_PROPOSAL
                        )
                    )
        }

}

enum class RefereeType {
    FIRST,
    SECOND
}

data class ForceRefereeRequest(
    val refereeId: Long
)