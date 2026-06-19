package es.uib.tfg.sports_league_backend.result.domain.match_event

import es.uib.tfg.sports_league_backend.result.domain.MatchPeriod
import es.uib.tfg.sports_league_backend.team.domain.Team
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import java.sql.Time

@Entity
@Table(name = "time_out")
@PrimaryKeyJoinColumn(name = "match_event_id")
class Timeout(
    happenedAtTime: Time,
    atLocalScore: Int,
    atVisitorScore: Int,
    period: MatchPeriod,
    triggerTeam: Team?,

    @Column(name = "duration_time", nullable = false)
    var durationTime: Time,
): MatchEvent(
    happenedAtTime = happenedAtTime,
    atLocalScore = atLocalScore,
    atVisitorScore = atVisitorScore,
    period = period,
    triggerTeam = triggerTeam
) {
}