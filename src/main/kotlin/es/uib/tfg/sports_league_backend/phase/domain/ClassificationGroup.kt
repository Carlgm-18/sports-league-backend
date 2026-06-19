package es.uib.tfg.sports_league_backend.phase.domain

import es.uib.tfg.sports_league_backend.team.domain.Team
import jakarta.persistence.*

@Entity
@Table(name = "classification_group")
class ClassificationGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(name = "top_winners", nullable = false)
    var topWinners: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    var phase: ClassificationPhase? = null,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    var groupTeams: MutableSet<ClassificationGroupTeam> = mutableSetOf()
) {
    var teams: List<Team> = listOf()
        get() = groupTeams.map { it.team }
}