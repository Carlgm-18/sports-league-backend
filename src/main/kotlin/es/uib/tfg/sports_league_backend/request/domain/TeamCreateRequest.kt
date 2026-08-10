package es.uib.tfg.sports_league_backend.request.domain

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.participant.infrastructure.repository.ParticipantJPAEntity
import es.uib.tfg.sportsapi.dto.RequestState
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "team_create_request")
@PrimaryKeyJoinColumn(name = "request_id")
class TeamCreateRequest(
    league: LeagueJPAEntity,
    participant: ParticipantJPAEntity,
    createdAt: LocalDateTime = LocalDateTime.now(),
    resolvedAt: LocalDateTime? = null,
    rejectionReason: String? = null,
    status: RequestState = RequestState.PENDING,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 10)
    var initials: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 255)
    var motto: String? = null,

    @Column(name = "primary_color", length = 7)
    var primaryColor: String = "#FFFFFF",

    @Column(name = "secondary_color", length = 7)
    var secondaryColor: String = "#000000",

    @Column(name = "icon_image_url", length = 255)
    var iconImageUrl: String? = null
) : Request(
    league = league,
    participant = participant,
    createdAt = createdAt,
    resolvedAt = resolvedAt,
    rejectionReason = rejectionReason,
    status = status
)
