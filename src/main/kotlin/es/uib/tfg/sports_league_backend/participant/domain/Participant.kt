package es.uib.tfg.sports_league_backend.participant.domain

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.user.domain.User
import java.time.LocalDateTime

class Participant(
    var id: Long? = null,
    var user: User,
    var roles: MutableSet<ParticipantRole> = mutableSetOf(),
    var league: League,
    var team: Team? = null,
    var dorsal: Int? = null,
    var joinDate: LocalDateTime = LocalDateTime.now(),
    var availability: MutableList<ParticipantAvailability> = mutableListOf(),
) {
    var availabilitySlots: List<DateTimeSlot> = listOf()
        get() = availability.map { it.dateTimeSlot }

    fun getRoundAvailability(roundId: Long) =
        availabilitySlots.filter { it.id == roundId }
}