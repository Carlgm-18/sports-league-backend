package es.uib.tfg.sports_league_backend.user.infrastructure.repository

import es.uib.tfg.sports_league_backend.user.domain.User
import es.uib.tfg.sports_league_backend.user.domain.RefereeLicense

fun User.toJPAEntity(): UserJPAEntity {
    val entity = UserJPAEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        passwordHash = passwordHash,
        category = category,
        createdAt = createdAt,
        profileImageUrl = profileImageUrl,
        signature = signature
    )
    entity.licenses = licenses.map { it.toJPAEntity() }.toMutableSet()
    return entity
}

fun UserJPAEntity.toDomain(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        passwordHash = passwordHash,
        category = category,
        createdAt = createdAt,
        profileImageUrl = profileImageUrl,
        licenses = licenses.map { it.toDomain() }.toMutableSet(),
        signature = signature
    )
}

fun RefereeLicense.toJPAEntity(): RefereeLicenseJPAEntity {
    return RefereeLicenseJPAEntity(
        id = id,
        license = license,
        uploadAt = uploadAt,
        sport = sport
    )
}

fun RefereeLicenseJPAEntity.toDomain(): RefereeLicense {
    return RefereeLicense(
        id = id,
        license = license,
        uploadAt = uploadAt,
        sport = sport
    )
}
