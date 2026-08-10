package es.uib.tfg.sports_league_backend.league.application.ports.`in`

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.errors.LeagueJoinError
import es.uib.tfg.sports_league_backend.participant.domain.Participant

interface JoinLeagueUseCase {
    fun joinLeague(leagueId: Long, userId: Long): DomainResult<Participant, LeagueJoinError>
}
