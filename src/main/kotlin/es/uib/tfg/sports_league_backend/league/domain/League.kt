package es.uib.tfg.sports_league_backend.league.domain

import es.uib.tfg.sports_league_backend.phase.domain.Phase
import es.uib.tfg.sports_league_backend.punctuation.domain.PunctuationSystem
import es.uib.tfg.sportsapi.dto.LeagueState
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "league")
class League(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id", nullable = false)
    var configuration: LeagueConfiguration,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "punctuation_system_id", nullable = false)
    var punctuationSystem: PunctuationSystem,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    var iconImageUrl: String? = null,

    var bannerImageUrl: String? = null,

    var locationUrl: String? = null,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var maxInscriptionDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LeagueState = LeagueState.TEAM_ASSEMBLE,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var deletedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "league", cascade = [CascadeType.ALL], orphanRemoval = true)
    var phases: MutableList<Phase> = mutableListOf(),
)