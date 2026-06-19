package es.uib.tfg.sports_league_backend.match.domain

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.round.domain.Round
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.MatchState
import jakarta.persistence.*

@Entity
@Table(name = "match")
class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var roundId: Long,

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
    var status: MatchState = MatchState.NOT_SCHEDULED,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datetime_slot_id")
    var dateTime: DateTimeSlot? = null,
)