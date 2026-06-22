package es.uib.tfg.sports_league_backend.participant.domain

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "participant")
class Participant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    var user: User,

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var roles: MutableSet<ParticipantRole>,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    var league: League,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null,

    var dorsal: Int? = null,

    @Column(nullable = false)
    var joinDate: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "participant", orphanRemoval = true, cascade = [CascadeType.ALL])
    var availability: MutableList<ParticipantAvailability> = mutableListOf(),
) {

    @Transient
    var availabilitySlots: List<DateTimeSlot> = listOf()
        get() = availability.map { it.dateTimeSlot }

    fun getRoundAvailability(roundId: Long) =
        availabilitySlots.filter { it.id == roundId }

}