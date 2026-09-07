package es.uib.tfg.sports_league_backend.team.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.team.application.TeamService
import es.uib.tfg.sports_league_backend.team.domain.error.TeamNotFound
import es.uib.tfg.sports_league_backend.request.domain.errors.TeamNotFound as RequestTeamNotFound
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sports_league_backend.participant.application.ports.`in`.ManageParticipantUseCase
import es.uib.tfg.sports_league_backend.request.application.RequestService
import es.uib.tfg.sports_league_backend.request.infrastructure.mapper.toDTO
import es.uib.tfg.sports_league_backend.participant.application.ParticipantService
import es.uib.tfg.sports_league_backend.participant.domain.errors.NotInATeam
import es.uib.tfg.sports_league_backend.participant.domain.errors.ParticipantNotFound
import es.uib.tfg.sports_league_backend.request.domain.errors.ParticipantNotFound as RequestParticipantNotFound
import es.uib.tfg.sports_league_backend.participant.domain.errors.UnauthorizedAction
import es.uib.tfg.sports_league_backend.request.domain.errors.UnauthorizedAction as RequestUnauthorizedAction
import es.uib.tfg.sports_league_backend.request.domain.errors.ParticipantAlreadyInATeam
import es.uib.tfg.sports_league_backend.request.domain.errors.ParticipantAndTeamLeagueMissmatch
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class TeamController(
    private val teamService: TeamService,
    private val manageParticipantUseCase: ManageParticipantUseCase,
    private val requestService: RequestService,
    private val participantService: ParticipantService
) {

    @GetMapping("/leagues/{leagueId}/teams")
    fun getTeamsByLeague(@PathVariable leagueId: Long) =
        ResponseEntity.ok(teamService.findAllByLeagueId(leagueId).map { it.toDetailsDTO() })

    @GetMapping("/teams/{teamId}")
    fun getTeamDetails(@PathVariable teamId: Long): ResponseEntity<*> {
        return when(val result = teamService.findById(teamId)) {
            is DomainResult.Success ->
                ResponseEntity.ok(result.data.toDetailsDTO())

            is DomainResult.Failure ->
                when(result.error) {
                    TeamNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "team"
                                )
                            )
                }
        }
    }

    @DeleteMapping("/teams/{teamId}")
    fun deleteTeam(
        @PathVariable teamId: Long,
        @AuthenticationPrincipal principal: Long
    ): ResponseEntity<*> {
        val team = when (val result = teamService.findById(teamId)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure ->
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        mapOf(
                            "error" to ErrorCode.RESOURCE_NOT_FOUND,
                            "resource" to "team"
                        )
                    )
        }

        val leagueId = team.league.id!!
        val participant = manageParticipantUseCase.findParticipant(principal, leagueId)
            .let { (it as? DomainResult.Success)?.data }

        val isCaptain = participant?.team?.id == teamId && participant.roles.any { it.participationRole.roleName == "CAPTAIN" }
        val isAdmin = participant?.roles?.any { it.participationRole.roleName == "ADMIN" } == true

        if (!isCaptain && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to ErrorCode.UNAUTHORIZED_ERROR))
        }

        teamService.deleteTeam(teamId)
        return ResponseEntity.noContent().build<Any>()
    }

    @DeleteMapping("/teams/{teamId}/members/{participantId}")
    fun kickTeamMember(
        @PathVariable teamId: Long,
        @PathVariable participantId: Long,
        @AuthenticationPrincipal principal: Long
    ): ResponseEntity<*> {
        return when (val result = participantService.removeMemberFromTeam(principal, teamId, participantId)) {
            is DomainResult.Success ->
                ResponseEntity.noContent().build<Any>()
            is DomainResult.Failure ->
                when (result.error) {
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
                    else ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .build<Any>()
                }
        }
    }

    @PostMapping("/teams/{teamId}/invitations")
    fun invitePlayer(
        @PathVariable teamId: Long,
        @RequestBody requestBody: Map<String, Long>,
        @AuthenticationPrincipal principal: Long
    ): ResponseEntity<*> {
        val participantId = requestBody["participantId"]
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "participantId is required"))

        return when (val result = requestService.inviteParticipantToTeam(principal, teamId, participantId)) {
            is DomainResult.Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.data.toDTO())
            is DomainResult.Failure ->
                when (result.error) {
                    is RequestTeamNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "team"
                                )
                            )
                    is RequestParticipantNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.RESOURCE_NOT_FOUND,
                                    "resource" to "participant"
                                )
                            )
                    is RequestUnauthorizedAction ->
                        ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.UNAUTHORIZED_ERROR
                                )
                            )
                    is ParticipantAndTeamLeagueMissmatch ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.SUBRESOURCE_MISMATCH,
                                    "subresource" to "league",
                                    "resources" to listOf("participant", "team")
                                )
                            )
                    is ParticipantAlreadyInATeam ->
                        ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(
                                mapOf(
                                    "error" to ErrorCode.ALREADY_IN_A_TEAM
                                )
                            )
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build<Any>()
                }
        }
    }
}