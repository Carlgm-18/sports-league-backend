package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import jakarta.persistence.*

@Entity
@Table(name = "participation_role")
class ParticipationRoleJPAEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var roleName: String
)
