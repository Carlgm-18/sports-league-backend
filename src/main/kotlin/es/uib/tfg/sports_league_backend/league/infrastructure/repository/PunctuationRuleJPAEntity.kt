package es.uib.tfg.sports_league_backend.league.infrastructure.repository

import jakarta.persistence.*

@Entity
@Table(name = "punctuation_rule")
class PunctuationRuleJPAEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "punctuation_system_id", nullable = false)
    var punctuationSystem: PunctuationSystemJPAEntity? = null,

    @Column(nullable = false)
    var localScore: Int,

    @Column(nullable = false)
    var visitorScore: Int,

    @Column(nullable = false)
    var localPoints: Int,

    @Column(nullable = false)
    var visitorPoints: Int
)
