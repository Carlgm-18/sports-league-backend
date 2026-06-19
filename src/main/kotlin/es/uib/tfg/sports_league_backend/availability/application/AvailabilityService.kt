package es.uib.tfg.sports_league_backend.availability.application

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.availability.domain.errors.AvailabilityRetrieveError
import es.uib.tfg.sports_league_backend.availability.domain.errors.AvailabilitySlotNotFound
import es.uib.tfg.sports_league_backend.availability.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.availability.infrastructure.repository.AvailabilityRepository
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sportsapi.dto.DateTimeSlotDetails
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AvailabilityService(
    private val availabilityRepository: AvailabilityRepository
) {

    @Transactional
    fun saveAvailability(
        roundId: Long,
        availability: List<DateTimeSlotDetails>
    ): DomainResult.Success<List<DateTimeSlot>> {

        // Delete previous availability
        availabilityRepository.deleteAvailabilityByRoundId(roundId)

        // Save new one
        val savedAvailability = availabilityRepository.saveAll(availability.map { it.toEntity(roundId) })
        return DomainResult.Success(savedAvailability)
    }

    fun findRoundAvailability(roundId: Long): DomainResult.Success<List<DateTimeSlot>> =
        DomainResult.Success(availabilityRepository.findAllAvailabilityByRoundId(roundId))

    fun findDateTimeSlotById(availabilitySlotId: Long): DomainResult<DateTimeSlot, AvailabilityRetrieveError> =
        availabilityRepository.findByIdOrNull(availabilitySlotId)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(AvailabilitySlotNotFound)

}