package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import es.uib.tfg.sportsapi.dto.LeagueCategory
import jakarta.persistence.*

@Entity
@Table(name = "league_configuration")
class LeagueConfigurationJPAEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: LeagueCategory,

    var minTeamFemaleIntegrants: Int? = null,

    @Column(nullable = false)
    var minTeamMembers: Int,

    @Column(nullable = false)
    var maxTeamMembers: Int,

    @Column(nullable = false)
    var roundDuration: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    var sport: Sport
)
