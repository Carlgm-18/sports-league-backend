package es.uib.tfg.sports_league_backend.sport.infrastructure.repository

import es.uib.tfg.sports_league_backend.sport.domain.Sport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SportRepository : JpaRepository<Sport, Int> {
    override fun findAll(): List<Sport>;
}