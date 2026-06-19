package es.uib.tfg.sports_league_backend.phase.infrastructure.repository

import es.uib.tfg.sports_league_backend.phase.domain.ClassificationGroup
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.LeaderboardProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClassificationGroupRepository : JpaRepository<ClassificationGroup, Long> {

    @Query(value = """
        SELECT 
            t.id AS teamId,
            COALESCE(SUM(p.played), 0) AS playedMatches,
            COALESCE(SUM(p.won), 0) AS wonMatches,
            COALESCE(SUM(p.lost), 0) AS lostMatches,
            COALESCE(SUM(p.draw), 0) AS drawnMatches,
            COALESCE(SUM(p.points), 0) AS points,
            COALESCE(SUM(p.won_sets), 0) AS wonSets,
            COALESCE(SUM(p.lost_sets), 0) AS lostSets,
            COALESCE(SUM(p.won_points), 0) AS wonPoints,
            COALESCE(SUM(p.lost_points), 0) AS lostPoints
        FROM team t
        JOIN classification_group_team cgt ON t.id = cgt.team_id
        LEFT JOIN v_match_team_performance p ON t.id = p.team_id
                                           AND p.phase_id = :phaseId
                                           AND p.round_sequence_order <= :roundSequenceOrder
        WHERE cgt.classification_group_id = :groupId
        GROUP BY t.id
    """, nativeQuery = true)
    fun getGroupLeaderboard(
        @Param("groupId") groupId: Long,
        @Param("phaseId") phaseId: Long,
        @Param("roundSequenceOrder") roundSequenceOrder: Int
    ): List<LeaderboardProjection>
}
