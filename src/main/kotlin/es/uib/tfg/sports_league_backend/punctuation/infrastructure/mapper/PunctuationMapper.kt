package es.uib.tfg.sports_league_backend.punctuation.infrastructure.mapper

import es.uib.tfg.sports_league_backend.punctuation.domain.PunctuationRule
import es.uib.tfg.sports_league_backend.punctuation.domain.PunctuationSystem
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toDetails
import es.uib.tfg.sports_league_backend.sport.infrastructure.mapper.toEntity
import es.uib.tfg.sportsapi.dto.PunctuationSystemDetails
import es.uib.tfg.sportsapi.dto.PunctuationSystemRuleDetails

fun PunctuationSystem.toDetailsDTO(): PunctuationSystemDetails =
    PunctuationSystemDetails(
        name,
        punctuationRules.map { it.toDetailsDTO() },
        sport.toDetails()
    )

fun PunctuationRule.toDetailsDTO(): PunctuationSystemRuleDetails =
    PunctuationSystemRuleDetails(
        localScore,
        visitorScore,
        localPoints,
        visitorPoints,
    )

fun PunctuationSystemRuleDetails.toEntity(): PunctuationRule =
    PunctuationRule(
        localScore = localScore,
        visitorScore = visitorScore,
        localPoints = localPoints,
        visitorPoints = visitorPoints,
    )

fun PunctuationSystemDetails.toEntity(): PunctuationSystem =
    PunctuationSystem(
        name = name,
        punctuationRules = rules.map { it.toEntity() } as MutableList<PunctuationRule>,
        sport = sport.toEntity(),
    )