package es.uib.tfg.sports_league_backend.result.domain

import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.result.domain.match_sign.MatchSign
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "result")
class Result(

    @Id
    var id: Long? = null,

    @Column(nullable = false)
    var localTotalScore: Long = 0,

    @Column(nullable = false)
    var visitorTotalScore: Long = 0,

    @Column(nullable = true)
    var recordUrl: URI? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "match_id")
    var match: Match,

    @OneToMany(mappedBy = "result", cascade = [CascadeType.ALL], orphanRemoval = true)
    var observations: MutableList<Observation> = mutableListOf(),

    @OneToMany(mappedBy = "result", cascade = [CascadeType.ALL], orphanRemoval = true)
    var matchPeriod: MutableList<MatchPeriod> = mutableListOf(),

    @OneToMany(mappedBy = "result", cascade = [CascadeType.ALL], orphanRemoval = true)
    var lineups: MutableList<Lineup> = mutableListOf(),

    @OneToMany(mappedBy = "result", cascade = [CascadeType.ALL], orphanRemoval = true)
    var matchSigns: MutableList<MatchSign> = mutableListOf(),

    )
