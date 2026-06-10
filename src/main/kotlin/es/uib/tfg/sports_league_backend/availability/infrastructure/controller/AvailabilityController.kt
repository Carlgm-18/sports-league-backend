package es.uib.tfg.sports_league_backend.availability.infrastructure.controller

import es.uib.tfg.sportsapi.dto.DateTimeSlotDetails
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rounds/{roundId}/availability")
class AvailabilityController {

    @GetMapping
    fun getRoundAvailability(@PathVariable roundId: String): ResponseEntity<*> {
        TODO("Not yet implemented")
    }

    @PostMapping
    fun registerRoundAvailability(
        @PathVariable roundId: String,
        @RequestBody availability: List<DateTimeSlotDetails>
    ): ResponseEntity<*> {
        TODO("Not yet implemented")
    }
}