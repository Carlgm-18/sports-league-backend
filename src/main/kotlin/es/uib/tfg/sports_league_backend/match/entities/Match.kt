package es.uib.tfg.sports_league_backend.match.entities

import es.uib.tfg.sports_league_backend.participant.entities.Participant
import es.uib.tfg.sports_league_backend.round.entities.Round
import es.uib.tfg.sports_league_backend.team.entities.Team
import es.uib.tfg.sportsapi.dto.MatchState
import jakarta.persistence.*

@Entity
@Table(name = "match")
class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    var round: Round,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_team_id")
    var localTeam: Team?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_team_id")
    var visitorTeam: Team?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_referee_id")
    var firstReferee: Participant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_referee_id")
    var secondReferee: Participant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MatchState = MatchState.NOT_SCHEDULED
)