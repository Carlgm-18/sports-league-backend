package es.uib.tfg.sports_league_backend.availability.infrastructure.repository

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AvailabilityRepository : JpaRepository<DateTimeSlot, Long> {
    fun findAllAvailabilityByRoundId(roundId: Long): List<DateTimeSlot>
}