package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.league.domain.LeagueConfiguration
import es.uib.tfg.sports_league_backend.league.domain.PunctuationSystem
import es.uib.tfg.sports_league_backend.league.domain.PunctuationRule
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.toJPAEntity
import es.uib.tfg.sports_league_backend.user.infrastructure.repository.toDomain

fun League.toJPAEntity(): LeagueJPAEntity {
    val entity = LeagueJPAEntity(
        id = id,
        configuration = configuration.toJPAEntity(),
        punctuationSystem = punctuationSystem.toJPAEntity(),
        owner = owner.toJPAEntity(),
        name = name,
        description = description,
        iconImageUrl = iconImageUrl,
        bannerImageUrl = bannerImageUrl,
        locationUrl = locationUrl,
        startDate = startDate,
        endDate = endDate,
        maxInscriptionDate = maxInscriptionDate,
        status = status,
        createdAt = createdAt,
        deletedAt = deletedAt,
        phases = phases
    )
    entity.phases.forEach { phase ->
        phase.league = entity
        if (phase is ClassificationPhase) {
            phase.groups.forEach { it.phase = phase }
        }
    }
    return entity
}

fun LeagueJPAEntity.toDomain(): League {
    return League(
        id = id,
        configuration = configuration.toDomain(),
        punctuationSystem = punctuationSystem.toDomain(),
        owner = owner.toDomain(),
        name = name,
        description = description,
        iconImageUrl = iconImageUrl,
        bannerImageUrl = bannerImageUrl,
        locationUrl = locationUrl,
        startDate = startDate,
        endDate = endDate,
        maxInscriptionDate = maxInscriptionDate,
        status = status,
        createdAt = createdAt,
        deletedAt = deletedAt,
        phases = phases
    )
}

fun LeagueConfiguration.toJPAEntity(): LeagueConfigurationJPAEntity {
    return LeagueConfigurationJPAEntity(
        id = id,
        name = name,
        category = category,
        minTeamFemaleIntegrants = minTeamFemaleIntegrants,
        minTeamMembers = minTeamMembers,
        maxTeamMembers = maxTeamMembers,
        roundDuration = roundDuration,
        sport = sport
    )
}

fun LeagueConfigurationJPAEntity.toDomain(): LeagueConfiguration {
    return LeagueConfiguration(
        id = id,
        name = name,
        category = category,
        minTeamFemaleIntegrants = minTeamFemaleIntegrants,
        minTeamMembers = minTeamMembers,
        maxTeamMembers = maxTeamMembers,
        roundDuration = roundDuration,
        sport = sport
    )
}

fun PunctuationSystem.toJPAEntity(): PunctuationSystemJPAEntity {
    val entity = PunctuationSystemJPAEntity(
        id = id,
        name = name,
        sport = sport
    )
    entity.punctuationRules = punctuationRules.map { it.toJPAEntity(entity) }.toMutableList()
    return entity
}

fun PunctuationSystemJPAEntity.toDomain(): PunctuationSystem {
    val domain = PunctuationSystem(
        id = id,
        name = name,
        sport = sport
    )
    domain.punctuationRules = punctuationRules.map { it.toDomain(domain) }.toMutableList()
    return domain
}

fun PunctuationRule.toJPAEntity(systemEntity: PunctuationSystemJPAEntity): PunctuationRuleJPAEntity {
    return PunctuationRuleJPAEntity(
        id = id,
        punctuationSystem = systemEntity,
        localScore = localScore,
        visitorScore = visitorScore,
        localPoints = localPoints,
        visitorPoints = visitorPoints
    )
}

fun PunctuationRuleJPAEntity.toDomain(systemDomain: PunctuationSystem): PunctuationRule {
    return PunctuationRule(
        id = id,
        punctuationSystem = systemDomain,
        localScore = localScore,
        visitorScore = visitorScore,
        localPoints = localPoints,
        visitorPoints = visitorPoints
    )
}
