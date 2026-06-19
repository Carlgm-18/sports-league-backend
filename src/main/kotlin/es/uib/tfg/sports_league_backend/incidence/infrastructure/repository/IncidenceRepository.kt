package es.uib.tfg.sports_league_backend.incidence.infrastructure.repository

import es.uib.tfg.sports_league_backend.incidence.domain.Incidence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IncidenceRepository : JpaRepository<Incidence,Long>