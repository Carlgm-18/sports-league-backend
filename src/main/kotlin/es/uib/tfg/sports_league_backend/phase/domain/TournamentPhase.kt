package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
@DiscriminatorValue("TOURNAMENT")
class TournamentPhase(
    league: League,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    sequenceOrder: Int
) : Phase(league = league, name = name, startDate = startDate, endDate = endDate, sequenceOrder = sequenceOrder) {

    @OneToMany(mappedBy = "phase", cascade = [CascadeType.ALL], orphanRemoval = true)
    var matchesOrder: MutableSet<TournamentSlot> = HashSet()

    var stagesNumber: Int = 1

}