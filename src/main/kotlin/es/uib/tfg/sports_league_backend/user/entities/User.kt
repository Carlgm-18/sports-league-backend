package es.uib.tfg.sports_league_backend.user.entities

import es.uib.tfg.sportsapi.dto.SignImageUrl
import es.uib.tfg.sportsapi.dto.UserCategory
import es.uib.tfg.sportsapi.dto.UserCreateRequestLicensesInner
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(nullable = false, length = 50)
    var firstName: String,

    @Column(nullable = false, length = 50)
    var lastName: String,

    @Column(nullable = false, unique = true, length = 50)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var category: UserCategory,

    // var pushToken: String? = null,

    @Column(name = "push_token")
    var pushToken: String? = null,

    var profileImageUrl: String = "",

    @Transient
    var licenses: List<UserCreateRequestLicensesInner> = listOf(),

    @Transient
    var signature: SignImageUrl? = null,
)