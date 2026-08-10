package es.uib.tfg.sports_league_backend.league.application.ports.out

import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem

interface PunctuationSystemRepositoryPort {
    fun findById(id: Long): PunctuationSystem?
    fun save(system: PunctuationSystem): PunctuationSystem
}
