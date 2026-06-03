package es.uib.tfg.sports_league_backend.request.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sportsapi.dto.RequestState
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "league_request")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    var league: League,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    var participant: Participant,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var resolvedAt: LocalDateTime? = null,

    var rejectionReason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RequestState = RequestState.PENDING
)
