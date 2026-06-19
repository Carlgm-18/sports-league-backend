package es.uib.tfg.sports_league_backend.phase.infrastructure.repository

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Date

@Repository
interface PhaseRepository: JpaRepository<Phase, Long> {
    fun findAllByLeagueIdOrderBySequenceOrder(leagueId: Long): List<Phase>

    fun findFirstByLeagueIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        leagueId: Long, startDate: LocalDate, endDate: LocalDate
    ): Phase?

    fun findActivePhase(leagueId: Long, date: LocalDate): Phase? =
        findFirstByLeagueIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            leagueId, date, date
        )

    fun findFirstByLeagueIdAndStartDateGreaterThanEqualOrderByStartDate(leagueId: Long, startDate: LocalDate): Phase?

    fun findIncomingPhase(leagueId: Long, date: LocalDate): Phase? =
        findFirstByLeagueIdAndStartDateGreaterThanEqualOrderByStartDate(leagueId, date)

}