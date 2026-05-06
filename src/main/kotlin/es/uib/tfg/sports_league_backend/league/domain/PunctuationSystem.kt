package es.uib.tfg.sports_league_backend.league.domain;

import jakarta.persistence.*

@Entity
@Table(name = "punctuation_system")
class PunctuationSystem (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(nullable = false)
    var leagueId: Int,

    @Column(nullable = false)
    var localScore: Int,

    @Column(nullable = false)
    var visitorScore: Int,

    @Column(nullable = false)
    var localPoints: Int,

    @Column(nullable = false)
    var visitorPoints: Int
)
