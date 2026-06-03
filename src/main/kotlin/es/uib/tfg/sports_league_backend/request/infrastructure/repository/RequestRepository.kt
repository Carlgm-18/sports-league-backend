package es.uib.tfg.sports_league_backend.request.infrastructure.repository

import es.uib.tfg.sports_league_backend.request.domain.Request
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RequestRepository : JpaRepository<Request, Long>
