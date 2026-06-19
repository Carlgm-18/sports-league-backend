package es.uib.tfg.sports_league_backend.result.infrastructure.repository

import es.uib.tfg.sports_league_backend.result.domain.MatchPeriodType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchPeriodTypeRepository : JpaRepository<MatchPeriodType, Long> {
    fun findByPeriodTypeName(periodTypeName: String): MatchPeriodType?
}
