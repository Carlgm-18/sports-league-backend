package es.uib.tfg.sports_league_backend.sport.infrastructure.controller

import es.uib.tfg.sports_league_backend.sport.application.SportService
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toDetails
import es.uib.tfg.sportsapi.dto.SportDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SportController(
    private val sportService: SportService
) {

    @GetMapping("/sports")
    fun getAllSports(): List<SportDetails> =
            sportService.getAllSports()
                .map { it.toDetails() }


}