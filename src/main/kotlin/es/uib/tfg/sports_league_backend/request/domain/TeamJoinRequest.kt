package es.uib.tfg.sports_league_backend.request.domain

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantJPAEntity
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.RequestState
import es.uib.tfg.sportsapi.dto.TeamJoinRequest
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "team_join_request")
@PrimaryKeyJoinColumn(name = "request_id")
class TeamJoinRequest(
    league: LeagueJPAEntity,
    participant: ParticipantJPAEntity,
    createdAt: LocalDateTime = LocalDateTime.now(),
    resolvedAt: LocalDateTime? = null,
    rejectionReason: String? = null,
    status: RequestState = RequestState.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var way: TeamJoinRequest.Way,
) : Request(
    league = league,
    participant = participant,
    createdAt = createdAt,
    resolvedAt = resolvedAt,
    rejectionReason = rejectionReason,
    status = status
)
