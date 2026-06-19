package es.uib.tfg.sports_league_backend.participant.domain

import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "availability")
class ParticipantAvailability(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "participant_id")
    var participant: Participant,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "datetime_slot_id")
    var dateTimeSlot: DateTimeSlot

)

