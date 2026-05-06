package es.uib.tfg.sports_league_backend.league.infrastructure.controller

import es.uib.tfg.sports_league_backend.league.application.LeagueService
import es.uib.tfg.sportsapi.dto.ConfigurationDetails
import es.uib.tfg.sportsapi.dto.ConfigurationUpdateRequest
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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