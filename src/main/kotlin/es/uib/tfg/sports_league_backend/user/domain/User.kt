package es.uib.tfg.sports_league_backend.user.domain

import es.uib.tfg.sportsapi.dto.SignImageUrl
import es.uib.tfg.sportsapi.dto.UserCategory
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var firstName: String,

    @Column(nullable = false, length = 50)
    var lastName: String,

    @Column(nullable = false, unique = true, length = 50)
    var email: String,

    @Column(nullable = false)
    var passwordHash: SecurePassword,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var category: UserCategory,

    var createdAt: LocalDateTime,

    @Column
    var profileImageUrl: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "app_user_id")
    var licenses: MutableSet<RefereeLicense> = mutableSetOf(),

    @Transient
    var signature: SignImageUrl? = null,
)