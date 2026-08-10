package es.uib.tfg.sports_league_backend.participant.infrastructure.repository

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import jakarta.persistence.*

@Entity
@Table(name = "availability")
class ParticipantAvailabilityJPAEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "participant_id")
    var participant: ParticipantJPAEntity,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "datetime_slot_id")
    var dateTimeSlot: DateTimeSlot
)
