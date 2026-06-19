package es.uib.tfg.sports_league_backend.result.domain.match_event

import es.uib.tfg.sports_league_backend.result.domain.MatchPeriod
import es.uib.tfg.sports_league_backend.team.domain.Team
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.sql.Time

@Entity
@Table(name = "match_event")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class MatchEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var happenedAtTime: Time,

    @Column(nullable = false)
    var atLocalScore: Int,

    @Column(nullable = false)
    var atVisitorScore: Int,

    @ManyToOne
    @JoinColumn(name = "match_period_id", nullable = false)
    var period: MatchPeriod,

    @ManyToOne
    @JoinColumn(name = "trigger_team_id", nullable = true)
    var triggerTeam: Team? = null

)