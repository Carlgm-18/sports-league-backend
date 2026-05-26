package es.uib.tfg.sports_league_backend.participant.infrastructure.mapper

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import es.uib.tfg.sports_league_backend.team.infrastructure.mapper.toSummaryDTO
import es.uib.tfg.sportsapi.dto.ParticipantDetails
import es.uib.tfg.sportsapi.dto.ParticipantSummary
import es.uib.tfg.sportsapi.dto.ParticipationRoleDTO

fun Participant.toSummaryDTO(): ParticipantSummary =
    ParticipantSummary(
        id!!,
        "${user.firstName} ${user.lastName}",
        listOf(),
        team?.toSummaryDTO(),
        dorsal,
    )

fun Participant.toDetailsDTO(): ParticipantDetails =
    ParticipantDetails(
        id!!,
        user.id!!,
        league.id!!,
        roles.map { it.participationRole.toDTO() }.toList(),
        joinDate,
        team?.toSummaryDTO(),
        dorsal
    )

fun ParticipationRole.toDTO(): ParticipationRoleDTO =
    ParticipationRoleDTO.valueOf(roleName)