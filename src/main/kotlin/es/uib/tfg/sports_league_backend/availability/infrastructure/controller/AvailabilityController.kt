package es.uib.tfg.sports_league_backend.availability.infrastructure.controller

import es.uib.tfg.sports_league_backend.availability.application.AvailabilityService
import es.uib.tfg.sports_league_backend.availability.infrastructure.mapper.toDetailsDTO
import es.uib.tfg.sportsapi.dto.DateTimeSlotDetails
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rounds/{roundId}/availability")
class AvailabilityController(
    private val availabilityService: AvailabilityService
) {

    @GetMapping
    fun getRoundAvailability(@PathVariable roundId: Long): ResponseEntity<*> =
        availabilityService.findRoundAvailability(roundId)
        .let {
            ResponseEntity
                .ok(it.data.map { d -> d.toDetailsDTO() })
        }


    @PutMapping
    fun registerRoundAvailability(
        @PathVariable roundId: Long,
        @Valid @RequestBody availability: List<DateTimeSlotDetails>
    ): ResponseEntity<*> =
        availabilityService.saveAvailability(roundId, availability)
        .let {
            ResponseEntity
                .ok(it.data.map { d -> d.toDetailsDTO() })
        }

}