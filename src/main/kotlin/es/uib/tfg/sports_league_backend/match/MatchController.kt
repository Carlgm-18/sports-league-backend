package es.uib.tfg.sports_league_backend.match

import es.uib.tfg.sportsapi.dto.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class MatchController(
    //private val matchService: MatchService
) {

//    @GetMapping("/leagues/{leagueId}/matches")
//    fun getLeagueMatches(
//        @PathVariable leagueId: Int,
//        @RequestParam(required = false) phaseId: Int?,
//        @RequestParam(required = false) teamId: Int?,
//        @RequestParam(required = false) status: MatchState?
//    ): List<MatchDetails> {
//        return matchService.findMatches(leagueId, phaseId, teamId, status)
//    }
//
//    @GetMapping("/matches/{matchId}")
//    fun getMatchDetails(@PathVariable matchId: Int): MatchDetails {
//        return matchService.findById(matchId)
//    }
//
//    @PatchMapping("/matches/{matchId}/start")
//    fun startMatch(@PathVariable matchId: Int): MatchDetails {
//        return matchService.startMatch(matchId)
//    }
//
//    @PutMapping("/matches/{matchId}/result")
//    fun submitMatchResult(
//        @PathVariable matchId: Int,
//        @Valid @RequestBody result: ResultDetails
//    ) {
//        matchService.submitResult(matchId, result)
//    }
}