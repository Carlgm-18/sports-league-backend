package es.uib.tfg.sports_league_backend.availability.infrastructure.mapper

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sportsapi.dto.DateTimeSlotDetails

fun DateTimeSlot.toDetailsDTO(): DateTimeSlotDetails =
    DateTimeSlotDetails(
        dateTime,
        duration
    )