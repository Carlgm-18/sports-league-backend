package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.league.domain.League
import es.uib.tfg.sports_league_backend.round.domain.Round
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "phase")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "phase_type", discriminatorType = DiscriminatorType.STRING)
abstract class Phase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    var league: League,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var sequenceOrder: Int,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "phase")
    var rounds: MutableList<Round> = mutableListOf()
)