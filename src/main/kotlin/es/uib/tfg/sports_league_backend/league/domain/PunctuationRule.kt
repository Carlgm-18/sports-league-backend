package es.uib.tfg.sports_league_backend.league.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

// TODO: add table name
@Entity
@Table(name = "punctuation_rule")
class PunctuationRule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "punctuation_system_id", nullable = false)
    var punctuationSystem: PunctuationSystem? = null,

    @Column(nullable = false)
    var localScore: Int,

    @Column(nullable = false)
    var visitorScore: Int,

    @Column(nullable = false)
    var localPoints: Int,

    @Column(nullable = false)
    var visitorPoints: Int
)