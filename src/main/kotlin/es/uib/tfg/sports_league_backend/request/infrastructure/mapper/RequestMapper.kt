package es.uib.tfg.sports_league_backend.request.infrastructure.mapper

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.request.domain.RefereeRequest
import es.uib.tfg.sports_league_backend.request.domain.Request
import es.uib.tfg.sports_league_backend.request.domain.TeamCreateRequest
import es.uib.tfg.sports_league_backend.request.domain.TeamJoinRequest
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sportsapi.dto.BaseRequest
import es.uib.tfg.sportsapi.dto.BaseRequest.RequestType
import es.uib.tfg.sportsapi.dto.RefereeRequest as RefereeRequestDTO
import es.uib.tfg.sportsapi.dto.TeamCreateRequest as TeamCreateRequestDTO
import es.uib.tfg.sportsapi.dto.TeamJoinRequest as TeamJoinRequestDTO
import java.net.URI

fun Request.toDTO(): BaseRequest =
    when (this) {
        is RefereeRequest -> this.toDTO()
        is TeamCreateRequest -> this.toDTO()
        is TeamJoinRequest -> this.toDTO()
        else -> error("Unknown request type")
    }

fun RefereeRequest.toDTO(): RefereeRequestDTO =
    RefereeRequestDTO(
        requestId = id!!,
        requestType = RequestType.REFEREE,
        participantId = participant.id!!,
        leagueId = league.id!!,
        createdAt = createdAt,
        status = status,
        resolvedAt = resolvedAt,
        rejectionReason = rejectionReason
    )

fun TeamCreateRequest.toDTO(): TeamCreateRequestDTO =
    TeamCreateRequestDTO(
        requestId = id!!,
        requestType = RequestType.TEAM_CREATE,
        participantId = participant.id!!,
        leagueId = league.id!!,
        createdAt = createdAt,
        status = status,
        resolvedAt = resolvedAt,
        rejectionReason = rejectionReason,
        name = name,
        initials = initials,
        description = description ?: "",
        motto = motto ?: "",
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        iconImageUrl = iconImageUrl?.let { URI(it) }
    )

fun TeamJoinRequest.toDTO(): TeamJoinRequestDTO =
    TeamJoinRequestDTO(
        requestId = id!!,
        requestType = RequestType.TEAM_JOIN,
        participantId = participant.id!!,
        leagueId = league.id!!,
        createdAt = createdAt,
        status = status,
        resolvedAt = resolvedAt,
        rejectionReason = rejectionReason,
        teamId = team.id!!,
        playerId = participant.id!!,
        way = when (way) {
            TeamJoinRequestDTO.Way.APPLIANCE -> TeamJoinRequestDTO.Way.APPLIANCE
            TeamJoinRequestDTO.Way.INVITATION -> TeamJoinRequestDTO.Way.INVITATION
        }
    )

fun TeamJoinRequestDTO.toEntity(league: League, participant: Participant, team: Team) =
    TeamJoinRequest(
        league = league,
        participant = participant,
        team = team,
        way = way,
    )