package es.uib.tfg.sports_league_backend.sign.domain

import jakarta.persistence.*

@Entity
@Table(name = "sign")
class Sign (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false)
    signImageUrl: String,

    @Column(nullable = false)
    uploadAt: LocalDateTime,
)