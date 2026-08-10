package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import jakarta.persistence.*

@Entity
@Table(name = "participant_role")
class ParticipantRoleJPAEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "participant_id")
    var participant: ParticipantJPAEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participation_role_id")
    var participationRole: ParticipationRoleJPAEntity
)
