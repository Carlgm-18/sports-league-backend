package es.uib.tfg.sports_league_backend.sign.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "sign")
class Sign (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false)
    var signImageUrl: String,

    @Column(nullable = false)
    var uploadAt: LocalDateTime,
)