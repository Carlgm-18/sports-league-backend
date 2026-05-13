package es.uib.tfg.sports_league_backend.sport.domain

import jakarta.persistence.*

@Entity
@Table(name = "sport")
class Sport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var sportName: String,
)