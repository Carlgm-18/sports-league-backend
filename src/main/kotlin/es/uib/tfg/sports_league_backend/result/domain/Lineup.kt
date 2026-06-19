package es.uib.tfg.sports_league_backend.result.domain

import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "lineup")
class Lineup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team,

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    var participant: Participant,

    @Column(nullable = true)
    var matchDorsal: Int? = null,

    @ManyToOne
    @JoinColumn(name = "result_id", nullable = false)
    var result: Result

)

