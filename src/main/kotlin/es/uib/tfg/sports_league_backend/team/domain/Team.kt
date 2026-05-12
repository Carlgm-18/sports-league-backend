package es.uib.tfg.sports_league_backend.team.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "team")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    var league: League,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 10)
    var initials: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var motto: String? = null,

    var primaryColor: String? = null,

    var secondaryColor: String? = null,

    var iconImageUrl: String? = null,

    var deletedAt: LocalDateTime? = null
)