package es.uib.tfg.sports_league_backend.user.infrastructure.repository

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "referee_license")
class RefereeLicenseJPAEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var license: String,

    var uploadAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    var sport: Sport
)
