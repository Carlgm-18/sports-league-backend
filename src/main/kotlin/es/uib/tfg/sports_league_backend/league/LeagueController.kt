package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sportsapi.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/leagues")
class LeagueController(
    private val leagueService: LeagueService
) {

    @GetMapping
    fun getAllLeagues(): List<LeagueDetails> {
        return leagueService.findAll()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLeague(@Valid @RequestBody request: LeagueCreateRequest): LeagueDetails {
        return leagueService.createLeague(request)
    }

    @GetMapping("/{leagueId}")
    fun getLeague(@PathVariable leagueId: Int): LeagueDetails {
        return leagueService.findById(leagueId)
    }

    @PatchMapping("/{leagueId}/configuration")
    fun updateConfiguration(
        @PathVariable leagueId: Int,
        @Valid @RequestBody request: ConfigurationUpdateRequest
    ): ConfigurationDetails {
        return leagueService.updateConfiguration(leagueId, request)
    }
}