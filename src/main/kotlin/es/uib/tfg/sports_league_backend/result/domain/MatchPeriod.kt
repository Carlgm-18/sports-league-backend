package es.uib.tfg.sports_league_backend.result.domain

import es.uib.tfg.sports_league_backend.result.domain.match_event.MatchEvent
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "match_period")
class MatchPeriod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "period_type_id")
    var periodType: MatchPeriodType,

    @Column(nullable = false)
    var periodNumber: Int,

    @Column(nullable = false)
    var localScore: Int,

    @Column(nullable = false)
    var visitorScore: Int,

    @OneToMany(mappedBy = "period")
    var matchEvents: MutableList<MatchEvent> = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "match_id")
    var result: Result

)

