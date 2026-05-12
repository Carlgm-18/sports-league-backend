package es.uib.tfg.sports_league_backend.phase.domain

import jakarta.persistence.*

@Entity
@Table(name = "classification_group")
class ClassificationGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "top_winners", nullable = false)
    var topWinners: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    var phase: ClassificationPhase,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    var groupTeams: MutableSet<ClassificationGroupTeam> = HashSet()
)