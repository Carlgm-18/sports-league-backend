package es.uib.tfg.sports_league_backend.result.infrastructure.repository

import es.uib.tfg.sports_league_backend.result.domain.match_event.SanctionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SanctionTypeRepository : JpaRepository<SanctionType, Long> {
    fun findBySanctionTypeName(sanctionTypeName: String): SanctionType?
}
