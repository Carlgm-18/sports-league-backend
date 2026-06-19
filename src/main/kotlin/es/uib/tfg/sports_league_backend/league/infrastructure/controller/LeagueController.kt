package es.uib.tfg.sports_league_backend.league.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sports_league_backend.league.domain.errors.AlreadyJoin
import es.uib.tfg.sports_league_backend.league.domain.errors.CategoryMismatch
import es.uib.tfg.sports_league_backend.league.domain.errors.ConfigurationNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.InscriptionClosed
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueAlreadyEnded
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.PunctuationSystemNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.SportNotFound
import es.uib.tfg.sports_league_backend.league.domain.errors.UserNotFound
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.participant.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueState
import es.uib.tfg.sportsapi.dto.LeagueSummary
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueUpdateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueNotFoundForUpdate
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueInProgressDateUpdate
import es.uib.tfg.sportsapi.dto.LeagueUpdateRequest
import org.springframework.security.access.prepost.PreAuthorize
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/leagues")
class LeagueController(
    private val leagueService: LeagueService
) {

    @GetMapping
    fun getAllLeagues(): List<LeagueSummary> =
        leagueService.findAll().map { it.toSummaryDTO() }

    @PostMapping
    fun createLeague(
            @Valid @RequestBody request: LeagueCreateRequest,
            @AuthenticationPrincipal userId: Long
    ): ResponseEntity<*> {

        if(!(request.isValidPunctuationSystem() and request.isValidConfiguration()))
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(
                        mapOf(
                            "error" to ErrorCode.INSTANTIATE_ONLY_CUSTOM_OR_ID_FIELD,
                            "field" to if(!request.isValidPunctuationSystem()) "punctuationSystemId"
                                        else "configurationId"
                        )
                    )

        return when(val result = leagueService.createLeague(request, userId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to when(result.error) {
                                SportNotFound -> "sport"
                                ConfigurationNotFound -> "configuration"
                                PunctuationSystemNotFound -> "punctuationSystem"
                                UserNotFound -> "user"
                            }
                        )
                    )
        }
    }

    @GetMapping("/{leagueId}")
    fun getLeague(@PathVariable leagueId: Long): ResponseEntity<*> =
        when(val result = leagueService.findLeagueById(leagueId)) {

            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )
        }

    @PostMapping("/{leagueId}/participants")
    fun joinLeague(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<*> =
        when(val result = leagueService.joinLeague(leagueId, userId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                when(result.error) {
                    AlreadyJoin ->
                        ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.PARTICIPANT_ALREADY_EXISTS
                                )
                            )
                    CategoryMismatch ->
                        ResponseEntity
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.CATEGORY_MISMATCH
                                )
                            )
                    InscriptionClosed ->
                        ResponseEntity
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.INSCRIPTIONS_CLOSED
                                )
                            )
                    LeagueAlreadyEnded ->
                        ResponseEntity
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.LEAGUE_ENDED
                                )
                            )
                    LeagueNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "league"
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

    @GetMapping("/{leagueId}/participants")
    fun getLeagueParticipants(@PathVariable leagueId: Long): ResponseEntity<*> =
        when(val result = leagueService.findAllParticipantsByLeagueId(leagueId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.map { it.toSummaryDTO() })
            is DomainResult.Failure ->
                when(result.error) {
                    is LeagueNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "league"
                                )
                            )
                }
        }


    @PatchMapping("/{leagueId}/configuration")
    @PreAuthorize("@leagueSecurityGuard.isAdmin(principal, #leagueId)")
    fun updateConfiguration(
        @PathVariable leagueId: Long,
        @Valid @RequestBody request: ConfigurationUpdateRequest
    ): ConfigurationDetails =
        leagueService.updateConfiguration(leagueId, request)

    @PatchMapping("/{leagueId}")
    @PreAuthorize("@leagueSecurityGuard.isAdmin(principal, #leagueId)")
    fun updateLeague(
        @PathVariable leagueId: Long,
        @Valid @RequestBody request: LeagueUpdateRequest
    ): ResponseEntity<*> {
        return when(val result = leagueService.updateLeague(leagueId, request)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                mapError(result.error)
        }
    }

    @GetMapping("/{leagueId}/leaderboard")
    fun getLeaderboard(
        @PathVariable leagueId: Long,
        @RequestParam(required = false) phaseId: Long?,
        @RequestParam(required = false) roundId: Long?
    ): ResponseEntity<*> {
        return when (val result = leagueService.getLeaderboard(leagueId, phaseId, roundId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data)

            is DomainResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )
        }
    }

    private fun mapError(error: LeagueUpdateError): ResponseEntity<*> =
        when (error) {
            LeagueNotFoundForUpdate ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "league"
                        )
                    )
            LeagueInProgressDateUpdate ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        mapOf(
                            "error" to ErrorCode.CREATE_RESOURCE_ERROR,
                            "message" to "Cannot update dates of a league that is in progress or ended"
                        )
                    )
        }

    @PostMapping("/{leagueId}/start")
    fun startLeague(
        @PathVariable leagueId: Long
    ): ResponseEntity<*> =
        when(val result = leagueService.startLeague(leagueId)) {
            is DomainResult.Success ->
                ResponseEntity
                    .noContent()
                    .build<Unit>()

            is DomainResult.Failure ->
                when(result.error) {
                    is LeagueNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "league"
                                )
                            )
                }
        }

}