package es.uib.tfg.sports_league_backend.league.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueCreateError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueRetrieveError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueStartError
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueUpdateError
import es.uib.tfg.sportsapi.dto.LeagueCreateRequest
import es.uib.tfg.sportsapi.dto.LeagueUpdateRequest

interface ManageLeagueUseCase {
    fun findAll(): List<League>
    fun createLeague(request: LeagueCreateRequest, ownerId: Long): DomainResult<League, LeagueCreateError>
    fun findLeagueById(leagueId: Long): DomainResult<League, LeagueRetrieveError>
    fun updateLeague(leagueId: Long, request: LeagueUpdateRequest): DomainResult<League, LeagueUpdateError>
    fun startLeague(leagueId: Long): DomainResult<Unit, LeagueStartError>
    fun deleteLeague(leagueId: Long): DomainResult<Unit, LeagueRetrieveError>
    fun updatePunctuationSystem(leagueId: Long, rules: List<es.uib.tfg.sportsapi.dto.PunctuationSystemRuleDetails>): DomainResult<List<es.uib.tfg.sportsapi.dto.PunctuationSystemRuleDetails>, LeagueRetrieveError>
}
