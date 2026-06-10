package es.uib.tfg.sports_league_backend.availability.application

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.availability.infrastructure.mapper.toEntity
import es.uib.tfg.sports_league_backend.availability.infrastructure.repository.AvailabilityRepository
import es.uib.tfg.sports_league_backend.core.DomainResult
import es.uib.tfg.sportsapi.dto.DateTimeSlotDetails
import org.springframework.stereotype.Service

@Service
class AvailabilityService(
    val availabilityRepository: AvailabilityRepository
) {

    fun saveAvailability(
        roundId: Long,
        availability: List<DateTimeSlotDetails>
    ): DomainResult.Success<List<DateTimeSlot>> =
        DomainResult.Success(availabilityRepository.saveAll(availability.map { it.toEntity(roundId) }))

    fun findRoundAvailability(roundId: Long): DomainResult.Success<List<DateTimeSlot>> =
        DomainResult.Success(availabilityRepository.findAllAvailabilityByRoundId(roundId))

}