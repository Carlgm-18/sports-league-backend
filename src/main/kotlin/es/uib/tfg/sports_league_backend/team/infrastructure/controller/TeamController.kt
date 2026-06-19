package es.uib.tfg.sports_league_backend.team.infrastructure.controller

import es.uib.tfg.sports_league_backend.common.ErrorCode
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.team.application.TeamService
import es.uib.tfg.sports_league_backend.team.domain.error.TeamNotFound
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toDetailsDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class TeamController(
    private val teamService: TeamService
) {

    @GetMapping("/leagues/{leagueId}/teams")
    fun getTeamsByLeague(@PathVariable leagueId: Long) =
        ResponseEntity.ok(teamService.findAllByLeagueId(leagueId))

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

}