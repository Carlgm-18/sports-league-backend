package es.uib.tfg.sports_league_backend.match.domain

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.ProposalState
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "proposal")
class Proposal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    var match: Match,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datetime_slot_id", nullable = false)
    var dateTimeSlot: DateTimeSlot,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProposalState = ProposalState.PENDING,

    @Column(nullable = false, updatable = false)
    var proposedAt: LocalDateTime = LocalDateTime.now(),

    var resolvedAt: LocalDateTime? = null
)
