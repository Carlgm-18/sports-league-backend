package es.uib.tfg.sports_league_backend.team.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.team.domain.error.TeamNotFound
import es.uib.tfg.sports_league_backend.team.domain.error.TeamRetrieveError
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.TeamRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TeamService(val teamRepository: TeamRepository) {
    fun findAllByLeagueId(leagueId: Long) =
        teamRepository.findAllByLeagueId(leagueId)

    fun findById(teamId: Long): DomainResult<Team, TeamRetrieveError> =
        teamRepository.findByIdOrNull(teamId)
            ?.let { return DomainResult.Success(it) }
            ?: DomainResult.Failure(TeamNotFound)
}