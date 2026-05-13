package es.uib.tfg.sports_league_backend.punctuation.domain

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import jakarta.persistence.*

@Entity
@Table(name = "punctuation_system")
class PunctuationSystem (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @OneToMany(mappedBy = "punctuationSystem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var punctuationRules: MutableList<PunctuationRule> = mutableListOf(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "sport_id", insertable = false, updatable = false)
    var sport: Sport,
)
