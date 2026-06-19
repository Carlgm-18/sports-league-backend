package es.uib.tfg.sports_league_backend.round.domain

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.availability.domain.DateTimeSlot
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "round")
class Round(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    var phase: Phase,

    @Column(nullable = false)
    var firstDay: LocalDate,

    @OneToMany(cascade = [CascadeType.ALL])
    var availability: MutableList<DateTimeSlot> = mutableListOf(),

    @Column(nullable = false)
    var sequenceOrder: Int
)