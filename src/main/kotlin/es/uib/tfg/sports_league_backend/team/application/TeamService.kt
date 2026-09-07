package es.uib.tfg.sports_league_backend.team.application

import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.toJPAEntity
import es.uib.tfg.sports_league_backend.request.domain.TeamCreateRequest
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.team.domain.error.TeamCreateError
import es.uib.tfg.sports_league_backend.team.domain.error.TeamNotFound
import es.uib.tfg.sports_league_backend.team.domain.error.TeamRetrieveError
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.TeamRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TeamService(val teamRepository: TeamRepository) {
    fun findAllByLeagueId(leagueId: Long) =
        teamRepository.findAllByLeagueIdAndDeletedAtIsNull(leagueId)

    fun findById(teamId: Long): DomainResult<Team, TeamRetrieveError> =
        teamRepository.findByIdOrNull(teamId)
            ?.takeIf { it.deletedAt == null }
            ?.let { return DomainResult.Success(it) }
            ?: DomainResult.Failure(TeamNotFound)

    @Transactional
    fun deleteTeam(teamId: Long): DomainResult<Unit, TeamRetrieveError> {
        val team = teamRepository.findByIdOrNull(teamId)
            ?.takeIf { it.deletedAt == null }
            ?: return DomainResult.Failure(TeamNotFound)
        team.deletedAt = LocalDateTime.now()
        teamRepository.save(team)
        return DomainResult.Success(Unit)
    }

    fun createTeamWithRequest(request: TeamCreateRequest, league: League): DomainResult<Team, TeamCreateError> {
        val team = Team(
            league = league.toJPAEntity(),
            name = request.name,
            initials = request.initials,
            description = request.description,
            motto = request.motto,
            primaryColor = request.primaryColor,
            secondaryColor = request.secondaryColor,
            iconImageUrl = request.iconImageUrl
        )
        val savedTeam = teamRepository.save(team)

        return DomainResult.Success(savedTeam)
    }
}