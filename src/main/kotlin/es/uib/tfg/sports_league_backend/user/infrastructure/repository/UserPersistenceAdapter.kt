package es.uib.tfg.sports_league_backend.user.infrastructure.repository

import es.uib.tfg.sports_league_backend.user.application.ports.out.UserRepositoryPort
import es.uib.tfg.sports_league_backend.user.domain.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val userRepository: UserRepository
) : UserRepositoryPort {

    override fun save(user: User): User {
        val jpaEntity = user.toJPAEntity()
        val savedEntity = userRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: Long): User? {
        return userRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)?.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
}
