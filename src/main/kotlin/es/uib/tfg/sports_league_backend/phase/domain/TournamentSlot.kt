package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.match.domain.Match
import jakarta.persistence.*

@Entity
@Table(name = "tournament_slot")
class TournamentSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(nullable = false)
    var indexOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    var phase: TournamentPhase,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "match_id", unique = true, nullable = false)
    var match: Match
)