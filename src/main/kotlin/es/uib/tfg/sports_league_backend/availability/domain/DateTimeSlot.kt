package es.uib.tfg.sports_league_backend.availability.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "datetime_slot")
class DateTimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var dateTime: LocalDateTime,

    @Column(nullable = false)
    var duration: Int,

    @Column(nullable = false)
    var roundId: Long
)