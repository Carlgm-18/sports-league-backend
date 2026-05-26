package es.uib.tfg.sports_league_backend.league.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueCreateError
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.league.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import es.uib.tfg.sportsapi.dto.LeagueSummary
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
import org.springframework.web.bind.annotation.ResponseStatus
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
    @ResponseStatus(HttpStatus.CREATED)
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

    @PostMapping("/{leagueId}/request")
    fun createLeagueRequest(
        @PathVariable leagueId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: BaseRequest
    ): ResponseEntity<*> {
        return ResponseEntity.badRequest().body("Not implemented yet")
    }

    @PatchMapping("/{leagueId}/configuration")
    fun updateConfiguration(
        @PathVariable leagueId: Int,
        @Valid @RequestBody request: ConfigurationUpdateRequest
    ): ConfigurationDetails =
        leagueService.updateConfiguration(leagueId, request)

    @PatchMapping("/{leagueId}")
    fun updateLeague(
        @PathVariable leagueId: Long,
        @Valid @RequestBody request: LeagueUpdateRequest
    ) {}
}