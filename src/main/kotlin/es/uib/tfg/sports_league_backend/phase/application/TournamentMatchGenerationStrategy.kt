package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.match.application.MatchService
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.phase.domain.MatchGenerationStrategy
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentPhase
import es.uib.tfg.sports_league_backend.phase.domain.TournamentSlot
import es.uib.tfg.sports_league_backend.round.application.RoundService
import es.uib.tfg.sportsapi.dto.MatchState
import org.springframework.stereotype.Component

@Component
class TournamentMatchGenerationStrategy(
    private val roundService: RoundService,
    private val matchService: MatchService,
    private val tournamentSlotService: TournamentSlotService
) : MatchGenerationStrategy {

    override fun generate(phase: Phase) {
        if (phase !is TournamentPhase) return

        val stages = phase.stagesNumber
        if (stages <= 0) return

        // Generate and persist all rounds
        val roundDuration = phase.league.configuration.roundDuration
        val globalRounds = roundService.saveAll(generateRounds(phase, stages, roundDuration))

        // Create and persist all matches
        val matchesToSave = mutableListOf<Match>()
        val matchOrders = mutableListOf<Int>()
        
        var indexOrder = 1
        // Create 2^(stages - s) matches for each round s
        for (s in 1..stages) {
            val roundId = globalRounds[s - 1].id!!
            val matchesInStageCount = 1 shl (stages - s) // 2^(stages - s)

            for (i in 0 until matchesInStageCount) {
                // Initial match with null teams and status NOT_SCHEDULED
                matchesToSave.add(
                    Match(
                        roundId = roundId,
                        localTeam = null,
                        visitorTeam = null,
                        status = MatchState.NOT_SCHEDULED
                    )
                )
                matchOrders.add(indexOrder)
                indexOrder++
            }
        }
        
        val savedMatches = matchService.saveAll(matchesToSave)
        
        val slotsToSave = savedMatches.mapIndexed { idx, savedMatch ->
            TournamentSlot(
                indexOrder = matchOrders[idx],
                phase = phase,
                match = savedMatch
            )
        }
        tournamentSlotService.saveAll(slotsToSave)
    }
}
