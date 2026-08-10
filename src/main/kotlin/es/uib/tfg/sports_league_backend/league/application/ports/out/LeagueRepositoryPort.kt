package es.uib.tfg.sports_league_backend.league.application.ports.out

import es.uib.tfg.sports_league_backend.league.domain.League

interface LeagueRepositoryPort {
    fun findAll(): List<League>
    fun findById(id: Long): League?
    fun save(league: League): League
    fun existsById(id: Long): Boolean
}
