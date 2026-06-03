package es.uib.tfg.sports_league_backend.request.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sportsapi.dto.RequestState
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "referee_request")
@PrimaryKeyJoinColumn(name = "request_id")
class RefereeRequest(
    league: League,
    participant: Participant,
    createdAt: LocalDateTime = LocalDateTime.now(),
    resolvedAt: LocalDateTime? = null,
    rejectionReason: String? = null,
    status: RequestState = RequestState.PENDING
) : Request(
    league = league,
    participant = participant,
    createdAt = createdAt,
    resolvedAt = resolvedAt,
    rejectionReason = rejectionReason,
    status = status
)
