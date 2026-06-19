package es.uib.tfg.sports_league_backend.result.domain.match_sign

import es.uib.tfg.sports_league_backend.result.domain.Result
import es.uib.tfg.sports_league_backend.sign.domain.Sign
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "match_sign")
class MatchSign(
    @Id
    var id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var moment: Moment,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var inMatchRole: InMatchRole,

    @ManyToOne
    @JoinColumn(name = "sign_id")
    var sign: Sign,

    @Column(nullable = false)
    var signedAt: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "result_id")
    var result: Result

) {
}