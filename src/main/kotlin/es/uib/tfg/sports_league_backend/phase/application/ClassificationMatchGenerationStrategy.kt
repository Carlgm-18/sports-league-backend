package es.uib.tfg.sports_league_backend.phase.application

import es.uib.tfg.sports_league_backend.match.application.MatchService
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.domain.MatchGenerationStrategy
import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.round.application.RoundService
import es.uib.tfg.sports_league_backend.round.domain.Round
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.MatchState
import org.springframework.stereotype.Component

@Component
class ClassificationMatchGenerationStrategy(
    private val roundService: RoundService,
    private val matchService: MatchService
) : MatchGenerationStrategy {

    override fun generate(phase: Phase) {
        if (phase !is ClassificationPhase) return

        val groups = phase.groups
        // Generate round-robin match schedules for each group (ida y vuelta)
        val groupSchedules = groups.map { group ->
            val teams = group.groupTeams.map { it.team }
            generateFullTournament(teams)
        }

        // Find the maximum number of rounds across all groups
        val maxRounds = groupSchedules.maxOfOrNull { it.size } ?: 0
        if (maxRounds == 0) return

        val league = phase.league
        val roundDuration = league.configuration.roundDuration

        // Create and save global Round entities for the phase using the generateRounds helper
        val globalRounds = roundService.saveAll(generateRounds(phase, maxRounds, roundDuration))

        // Accumulate all Match entities to save them in bulk
        val matchesToSave = mutableListOf<Match>()
        for (groupIdx in groups.indices) {
            val schedule = groupSchedules[groupIdx]
            for (r in schedule.indices) {
                val roundMatches = schedule[r]
                val globalRoundId = globalRounds[r].id!!
                
                for (matchPair in roundMatches) {
                    matchesToSave.add(
                        Match(
                            roundId = globalRoundId,
                            localTeam = matchPair.first,
                            visitorTeam = matchPair.second,
                            status = MatchState.NOT_SCHEDULED
                        )
                    )
                }
            }
        }
        
        matchService.saveAll(matchesToSave)
    }

    private fun generateFullTournament(teams: List<Team>): List<List<Pair<Team, Team>>> {
        val list = teams.toMutableList<Team?>()

        // If the teams number is odd, we add a BYE team represented as null
        if (list.size % 2 != 0) {
            list.add(null)
        }

        val nTeams = list.size
        val nRounds = nTeams - 1
        val matchesPerRound = nTeams / 2
        val firstLegMatches = mutableListOf<List<Pair<Team, Team>>>()

        for (round in 0 until nRounds) {
            val roundMatches = mutableListOf<Pair<Team, Team>>()

            for (i in 0 until matchesPerRound) {
                val home = list[i]
                val away = list[nTeams - 1 - i]

                // If any of the teams is null it means that the team rests for that round
                if (home != null && away != null) {
                    // Alternate locality by round oddity to balance home and away matches
                    if (round % 2 == 0) {
                        roundMatches.add(home to away)
                    } else {
                        roundMatches.add(away to home)
                    }
                }
            }
            firstLegMatches.add(roundMatches)

            // Berger rotation: left one team fix and rotate the others
            list.add(1, list.removeLast())
        }

        // Clone first leg matches and invert the locality to generate the second leg matches
        val secondLegMatches = firstLegMatches.map { round ->
            round.map { match ->
                match.second to match.first
            }
        }

        // Concatenate both series
        return firstLegMatches + secondLegMatches
    }
}
