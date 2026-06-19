package es.uib.tfg.sports_league_backend.result.domain.match_event

import es.uib.tfg.sports_league_backend.result.domain.Lineup
import es.uib.tfg.sports_league_backend.result.domain.MatchPeriod
import es.uib.tfg.sports_league_backend.team.domain.Team
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import java.sql.Time

@Entity
@Table(name = "sanction")
@PrimaryKeyJoinColumn(name = "match_event_id")
class Sanction(
    happenedAtTime: Time,
    atLocalScore: Int,
    atVisitorScore: Int,
    period: MatchPeriod,
    triggerTeam: Team?,

    @ManyToOne
    @JoinColumn(name = "lineup_id")
    var appliedTo: Lineup,

    @ManyToOne
    @JoinColumn(name = "sanction_type_id")
    var sanctionType: SanctionType,

    var reason: String
): MatchEvent(
    happenedAtTime = happenedAtTime,
    atLocalScore = atLocalScore,
    atVisitorScore = atVisitorScore,
    period = period,
    triggerTeam = triggerTeam
) {
}