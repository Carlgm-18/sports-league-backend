package es.uib.tfg.sports_league_backend.request.infrastructure.repository

import es.uib.tfg.sports_league_backend.request.domain.TeamJoinRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamJoinRequestRepository : JpaRepository<TeamJoinRequest, Long> {
    fun findAllByTeamId(teamId: Long): List<TeamJoinRequest>
}
