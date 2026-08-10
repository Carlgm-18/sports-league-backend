package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.participant.domain.Participant
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantRole
import es.uib.tfg.sports_league_backend.participant.domain.ParticipationRole
import es.uib.tfg.sports_league_backend.participant.domain.ParticipantAvailability
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.toJPAEntity
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.toDomain
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.toJPAEntity
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.toDomain

fun Participant.toJPAEntity(): ParticipantJPAEntity {
    val entity = ParticipantJPAEntity(
        id = id,
        user = user.toJPAEntity(),
        league = league.toJPAEntity(),
        team = team,
        dorsal = dorsal,
        joinDate = joinDate
    )
    entity.roles = roles.map { it.toJPAEntity(entity) }.toMutableSet()
    entity.availability = availability.map { it.toJPAEntity(entity) }.toMutableList()
    return entity
}

fun ParticipantJPAEntity.toDomain(): Participant {
    val domain = Participant(
        id = id,
        user = user.toDomain(),
        league = league.toDomain(),
        team = team,
        dorsal = dorsal,
        joinDate = joinDate
    )
    domain.roles = roles.map { it.toDomain(domain) }.toMutableSet()
    domain.availability = availability.map { it.toDomain(domain) }.toMutableList()
    return domain
}

fun ParticipantRole.toJPAEntity(participantEntity: ParticipantJPAEntity): ParticipantRoleJPAEntity {
    return ParticipantRoleJPAEntity(
        id = id,
        participant = participantEntity,
        participationRole = participationRole.toJPAEntity()
    )
}

fun ParticipantRoleJPAEntity.toDomain(participantDomain: Participant): ParticipantRole {
    return ParticipantRole(
        id = id,
        participant = participantDomain,
        participationRole = participationRole.toDomain()
    )
}

fun ParticipationRole.toJPAEntity(): ParticipationRoleJPAEntity {
    return ParticipationRoleJPAEntity(
        id = id,
        roleName = roleName
    )
}

fun ParticipationRoleJPAEntity.toDomain(): ParticipationRole {
    return ParticipationRole(
        id = id,
        roleName = roleName
    )
}

fun ParticipantAvailability.toJPAEntity(participantEntity: ParticipantJPAEntity): ParticipantAvailabilityJPAEntity {
    return ParticipantAvailabilityJPAEntity(
        id = id,
        participant = participantEntity,
        dateTimeSlot = dateTimeSlot
    )
}

fun ParticipantAvailabilityJPAEntity.toDomain(participantDomain: Participant): ParticipantAvailability {
    return ParticipantAvailability(
        id = id,
        participant = participantDomain,
        dateTimeSlot = dateTimeSlot
    )
}
