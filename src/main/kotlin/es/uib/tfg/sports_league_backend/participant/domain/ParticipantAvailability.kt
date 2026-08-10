package es.uib.tfg.sports_league_backend.participant.domain

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot

class ParticipantAvailability(
    var id: Long? = null,
    var participant: Participant,
    var dateTimeSlot: DateTimeSlot
)
