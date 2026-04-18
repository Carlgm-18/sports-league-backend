package es.uib.tfg.sports_league_backend.participant.entities

import es.uib.tfg.sports_league_backend.league.entities.League
import es.uib.tfg.sports_league_backend.team.entities.Team
import es.uib.tfg.sports_league_backend.user.entities.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "participation")
class Participant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    var league: League,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null,

    var dorsal: Int? = null,

    @Column(nullable = false)
    var joinDate: LocalDateTime = LocalDateTime.now()
)