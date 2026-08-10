package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.UserJPAEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "participant")
class ParticipantJPAEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    var user: UserJPAEntity,

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var roles: MutableSet<ParticipantRoleJPAEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    var league: LeagueJPAEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null,

    var dorsal: Int? = null,

    @Column(nullable = false)
    var joinDate: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "participant", orphanRemoval = true, cascade = [CascadeType.ALL])
    var availability: MutableList<ParticipantAvailabilityJPAEntity> = mutableListOf(),
)
