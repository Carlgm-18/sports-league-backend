package es.uib.tfg.sports_league_backend.team.domain

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantJPAEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "team")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    var league: LeagueJPAEntity,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 10)
    var initials: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var motto: String? = null,

    var primaryColor: String = "#FFFFFF",

    var secondaryColor: String = "#000000",

    var iconImageUrl: String? = null,

    var deletedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL], orphanRemoval = true)
    var members: MutableList<ParticipantJPAEntity> = mutableListOf()

)