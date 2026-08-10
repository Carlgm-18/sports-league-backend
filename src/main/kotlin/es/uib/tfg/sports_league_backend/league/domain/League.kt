package es.uib.tfg.sports_league_backend.league.domain

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sportsapi.dto.LeagueState
import java.time.LocalDate
import java.time.LocalDateTime

class League(
    var id: Long? = null,
    var configuration: LeagueConfiguration,
    var punctuationSystem: PunctuationSystem,
    var owner: User,
    var name: String,
    var description: String = "",
    var iconImageUrl: String? = null,
    var bannerImageUrl: String? = null,
    var locationUrl: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var maxInscriptionDate: LocalDate? = null,
    var status: LeagueState = LeagueState.TEAM_ASSEMBLE,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var deletedAt: LocalDateTime? = null,
    var phases: MutableList<Phase> = mutableListOf(),
)