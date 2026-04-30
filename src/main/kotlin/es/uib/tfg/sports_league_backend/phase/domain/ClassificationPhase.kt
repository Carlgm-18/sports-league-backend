package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
@DiscriminatorValue("CLASSIFICATION")
class ClassificationPhase(
    id: Int = 0,
    league: League,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    sequenceOrder: Int
) :
    Phase(id, league, name, startDate, endDate, sequenceOrder) {
    // Aquí iría la relación con CLASSIFICATION_GROUP según el diagrama
}