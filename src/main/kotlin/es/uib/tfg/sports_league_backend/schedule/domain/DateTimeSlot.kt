package es.uib.tfg.sports_league_backend.schedule.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

// TODO: add table name
@Entity
class DateTimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var dateTime: LocalDateTime,

    @Column(nullable = false)
    var duration: Int
)