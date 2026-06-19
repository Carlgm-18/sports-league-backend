package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.round.domain.Round

interface MatchGenerationStrategy {
    fun generate(phase: Phase)

    fun generateRounds(phase: Phase, rounds: Int, roundDuration: Int): List<Round> {
        val globalRounds = mutableListOf<Round>()
        for (r in 0 until rounds) {
            val roundDate = phase.startDate.plusWeeks(r.toLong() * roundDuration)
            val round = Round(
                phase = phase,
                firstDay = roundDate,
                sequenceOrder = r + 1
            )
            globalRounds.add(round)
        }
        return globalRounds
    }
}
