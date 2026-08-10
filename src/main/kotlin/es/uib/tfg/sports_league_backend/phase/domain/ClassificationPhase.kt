package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueJPAEntity
import es.uib.tfg.sports_league_backend.team.domain.Team
import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
@DiscriminatorValue("CLASSIFICATION")
class ClassificationPhase(
    league: LeagueJPAEntity,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    sequenceOrder: Int,
    groups: List<ClassificationGroup>,
) : Phase(league = league, name = name, startDate = startDate, endDate = endDate, sequenceOrder = sequenceOrder) {

    @OneToMany(mappedBy = "phase", cascade = [CascadeType.ALL], orphanRemoval = true)
    var groups: MutableList<ClassificationGroup> = groups.toMutableList()
}