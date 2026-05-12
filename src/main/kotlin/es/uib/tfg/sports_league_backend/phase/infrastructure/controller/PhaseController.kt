package es.uib.tfg.sports_league_backend.phase.infrastructure.controller

import es.uib.tfg.sports_league_backend.phase.application.PhaseService
import es.uib.tfg.sportsapi.dto.PhaseCreateRequest
import es.uib.tfg.sportsapi.dto.PhaseDetails
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/phases")
class PhaseController(
    private val phaseService: PhaseService,
//    private val roundService: RoundService
) {

    @PostMapping
    fun createLeaguePhases(@PathVariable leagueId: String, @RequestBody request: PhaseCreateRequest) {

    }
//
//    // --- FASES ---
//    @GetMapping("/{phaseId}")
//    fun getPhaseDetails(
//        @PathVariable leagueId: Int,
//        @PathVariable phaseId: Int
//    ): PhaseDetails { // Devuelve ClassificationPhaseDetails o TournamentPhaseDetails por el polimorfismo
//        return phaseService.getPhaseDetails(leagueId, phaseId)
//    }
//
//    // --- RONDAS / DISPONIBILIDAD ---
//    @GetMapping("/{phaseId}/rounds/{roundId}/availability")
//    fun getRoundAvailability(
//        @PathVariable leagueId: Int,
//        @PathVariable phaseId: Int,
//        @PathVariable roundId: Int
//    ): List<DateTimeSlot> {
//        return roundService.getLocationAvailability(leagueId, phaseId, roundId)
//    }
//
//    @PutMapping("/{phaseId}/rounds/{roundId}/availability")
//    fun setRoundAvailability(
//        @PathVariable leagueId: Int,
//        @PathVariable phaseId: Int,
//        @PathVariable roundId: Int,
//        @RequestBody slots: List<DateTimeSlot>
//    ) {
//        roundService.setLocationAvailability(leagueId, phaseId, roundId, slots)
//    }
}