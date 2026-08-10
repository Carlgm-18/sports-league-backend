package es.uib.tfg.sports_league_backend.incidence.domain

import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantJPAEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "incidence")
class Incidence(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id : Long? = null,

    @Column(nullable = false)
    var description : String,

    var resolution: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    var creator: ParticipantJPAEntity,

    @Column(nullable = false)
    var leagueId: Long
)