package es.uib.tfg.sports_league_backend.team

import es.uib.tfg.sportsapi.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class TeamController(
    //private val teamService: TeamService
) {
//
//    // Rutas que cuelgan de League
//    @PostMapping("/leagues/{leagueId}/teams")
//    @ResponseStatus(HttpStatus.CREATED)
//    fun requestTeamCreation(
//        @PathVariable leagueId: Int,
//        @Valid @RequestBody request: TeamCreateRequest
//    ) {
//        teamService.requestCreation(leagueId, request)
//    }
//
//    @GetMapping("/leagues/{leagueId}/teams")
//    fun getTeamsByLeague(@PathVariable leagueId: Int): List<TeamDetails> {
//        return teamService.findByLeagueId(leagueId)
//    }
//
//    // Rutas directas del Equipo
//    @GetMapping("/teams/{teamId}")
//    fun getTeamDetails(@PathVariable teamId: Int): TeamDetails { // El DTO unificado usando allOf
//        return teamService.getTeamDetails(teamId)
//    }
//
//    @PostMapping("/teams/{teamId}/join-requests")
//    @ResponseStatus(HttpStatus.CREATED)
//    fun requestToJoinTeam(
//        @PathVariable teamId: Int,
//        @Valid @RequestBody request: TeamJoinRequest
//    ) {
//        teamService.requestToJoin(teamId, request)
//    }
}