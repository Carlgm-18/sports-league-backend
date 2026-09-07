package es.uib.tfg.sports_league_backend.league

import es.uib.tfg.sports_league_backend.BaseIntegrationTest
import es.uib.tfg.sports_league_backend.league.infrastructure.repository.LeagueRepository
import es.uib.tfg.sports_league_backend.match.domain.Match
import es.uib.tfg.sports_league_backend.match.infrastructure.repository.MatchRepository
import es.uib.tfg.sports_league_backend.phase.domain.ClassificationPhase
import es.uib.tfg.sports_league_backend.phase.infrastructure.repository.PhaseRepository
import es.uib.tfg.sports_league_backend.result.domain.MatchPeriod
import es.uib.tfg.sports_league_backend.result.domain.MatchPeriodType
import es.uib.tfg.sports_league_backend.result.domain.Result
import es.uib.tfg.sports_league_backend.result.infrastructure.repository.MatchPeriodTypeRepository
import es.uib.tfg.sports_league_backend.result.infrastructure.repository.ResultRepository
import es.uib.tfg.sports_league_backend.round.domain.Round
import es.uib.tfg.sports_league_backend.round.infrastructure.repository.RoundRepository
import es.uib.tfg.sports_league_backend.team.domain.Team
import es.uib.tfg.sports_league_backend.team.infrastructure.repository.TeamRepository
import es.uib.tfg.sportsapi.dto.MatchState
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import kotlin.math.pow
import kotlin.math.sqrt

class LeaderboardBenchmarkTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var leagueRepository: LeagueRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @Autowired
    private lateinit var resultRepository: ResultRepository

    @Autowired
    private lateinit var phaseRepository: PhaseRepository

    @Autowired
    private lateinit var matchPeriodTypeRepository: MatchPeriodTypeRepository

    @Test
    @DisplayName("Benchmark de tiempo de respuesta del endpoint de Leaderboard con vista materializada")
    fun benchmarkLeaderboardResponseTime() {
        val userToken = obtainToken(randomEmail())
        val leagueId = createLeague(userToken, "Liga Benchmark Voleibol")

        val leagueEntity = leagueRepository.findByIdOrNull(leagueId)
            ?: throw IllegalStateException("League $leagueId not found")

        val phase = phaseRepository.findAllByLeagueIdOrderBySequenceOrder(leagueId)
            .filterIsInstance<ClassificationPhase>()
            .first()
        val phaseId = phase.id!!
        val groupId = jdbcTemplate.queryForObject(
            "SELECT id FROM classification_group WHERE phase_id = ? LIMIT 1",
            Long::class.java,
            phaseId
        )!!

        // 1. Crear 10 equipos simulados en la liga
        val teamsCount = 10
        val teams = (1..teamsCount).map { i ->
            val team = teamRepository.save(
                Team(
                    league = leagueEntity,
                    name = "Club Voleibol Palma $i",
                    initials = "CVP$i",
                    description = "Equipo simulado de prueba número $i",
                    motto = "Pasión y esfuerzo $i",
                    primaryColor = "#%06X".format(i * 123456 % 0xFFFFFF),
                    secondaryColor = "#FFFFFF"
                )
            )
            // Asignar el equipo al grupo de clasificación
            jdbcTemplate.update(
                "INSERT INTO classification_group_team (classification_group_id, team_id) VALUES (?, ?)",
                groupId,
                team.id
            )
            team
        }

        // 2. Crear 10 jornadas con 5 partidos por jornada
        val periodType = matchPeriodTypeRepository.findByPeriodTypeName("SET")
            ?: matchPeriodTypeRepository.save(MatchPeriodType(periodTypeName = "SET"))

        val totalRounds = 10
        val createdRounds = mutableListOf<Round>()
        val matchesToSave = mutableListOf<Match>()

        for (r in 1..totalRounds) {
            val round = roundRepository.save(
                Round(
                    phase = phase,
                    firstDay = LocalDate.now().plusDays((r * 7).toLong()),
                    sequenceOrder = r
                )
            )
            createdRounds.add(round)

            // Emparejar los 10 equipos en 5 partidos por jornada usando rotación estándar
            for (matchIndex in 0 until (teamsCount / 2)) {
                val localIdx = (matchIndex + r - 1) % teamsCount
                val visitorIdx = (teamsCount - 1 - matchIndex + r - 1) % teamsCount

                val localTeam = teams[localIdx]
                val visitorTeam = teams[visitorIdx]

                matchesToSave.add(
                    Match(
                        roundId = round.id!!,
                        localTeam = localTeam,
                        visitorTeam = visitorTeam,
                        status = MatchState.ENDED
                    )
                )
            }
        }

        val savedMatches = matchRepository.saveAll(matchesToSave)

        // 3. Crear resultados para cada partido simulado
        for ((idx, match) in savedMatches.withIndex()) {
            val localWins = idx % 2 == 0 // Alternar victorias para crear tabla variada
            val localSets = if (localWins) 3 else (idx % 3)
            val visitorSets = if (!localWins) 3 else (idx % 3)

            jdbcTemplate.update(
                "INSERT INTO result (match_id, local_total_score, visitor_total_score) VALUES (?, ?, ?)",
                match.id,
                localSets,
                visitorSets
            )
        }

        // 4. Refrescar la vista materializada v_match_team_performance
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW v_match_team_performance")

        // 5. Verificación funcional previa
        mockMvc.perform(get("/api/v1/leagues/$leagueId/leaderboard"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].standings.length()").value(teamsCount))
            .andExpect(jsonPath("$[0].standings[0].position").value(1))

        // 6. Fase de calentamiento (Warmup)
        val warmupIterations = 15
        for (i in 1..warmupIterations) {
            mockMvc.perform(get("/api/v1/leagues/$leagueId/leaderboard"))
                .andExpect(status().isOk)
        }

        // 7. Medición de tiempo de respuesta (Benchmark principal: leaderboard completo)
        val benchmarkIterations = 100
        val fullLeaderboardLatencies = ArrayList<Double>(benchmarkIterations)

        for (i in 1..benchmarkIterations) {
            val start = System.nanoTime()
            mockMvc.perform(get("/api/v1/leagues/$leagueId/leaderboard"))
                .andExpect(status().isOk)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
            fullLeaderboardLatencies.add(elapsedMs)
        }

        // 8. Medición con filtro de jornada (Benchmark: leaderboard histórico hasta jornada 5)
        val midRoundId = createdRounds[4].id!!
        val filteredLatencies = ArrayList<Double>(benchmarkIterations)

        for (i in 1..benchmarkIterations) {
            val start = System.nanoTime()
            mockMvc.perform(get("/api/v1/leagues/$leagueId/leaderboard").param("roundId", midRoundId.toString()))
                .andExpect(status().isOk)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
            filteredLatencies.add(elapsedMs)
        }

        // 9. Cálculo de métricas estadísticas
        val fullStats = calculateStats(fullLeaderboardLatencies)
        val filteredStats = calculateStats(filteredLatencies)

        // 10. Imprimir informe detallado en consola
        printBenchmarkReport(
            leagueId = leagueId,
            teamsCount = teamsCount,
            totalMatches = savedMatches.size,
            warmupCount = warmupIterations,
            sampleCount = benchmarkIterations,
            fullStats = fullStats,
            filteredStats = filteredStats
        )

        // 11. Aserciones de rendimiento (SLA esperado en pruebas locales)
        assert(fullStats.avg < 100.0) {
            "El tiempo medio de respuesta debe ser < 100ms, pero fue ${fullStats.avg}ms"
        }
        assert(fullStats.p95 < 150.0) {
            "El percentil 95 debe ser < 150ms, pero fue ${fullStats.p95}ms"
        }
    }

    private data class LatencyStats(
        val count: Int,
        val min: Double,
        val max: Double,
        val avg: Double,
        val median: Double,
        val p90: Double,
        val p95: Double,
        val p99: Double,
        val stdDev: Double,
        val throughputRps: Double
    )

    private fun calculateStats(latencies: List<Double>): LatencyStats {
        val sorted = latencies.sorted()
        val count = sorted.size
        val min = sorted.first()
        val max = sorted.last()
        val avg = sorted.average()
        val median = percentile(sorted, 50.0)
        val p90 = percentile(sorted, 90.0)
        val p95 = percentile(sorted, 95.0)
        val p99 = percentile(sorted, 99.0)

        val variance = sorted.map { (it - avg).pow(2) }.average()
        val stdDev = sqrt(variance)
        val throughput = if (avg > 0) 1000.0 / avg else 0.0

        return LatencyStats(
            count = count,
            min = min,
            max = max,
            avg = avg,
            median = median,
            p90 = p90,
            p95 = p95,
            p99 = p99,
            stdDev = stdDev,
            throughputRps = throughput
        )
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        val index = (p / 100.0 * (sorted.size - 1)).toInt()
        return sorted[index]
    }

    private fun printBenchmarkReport(
        leagueId: Long,
        teamsCount: Int,
        totalMatches: Int,
        warmupCount: Int,
        sampleCount: Int,
        fullStats: LatencyStats,
        filteredStats: LatencyStats
    ) {
        println()
        println("=========================================================================================")
        println("                    BENCHMARK DE TIEMPO DE RESPUESTA: LEADERBOARD                        ")
        println("=========================================================================================")
        println(" Entorno y datos simulados:")
        println("   - ID de Liga:                  $leagueId")
        println("   - Equipos en el Grupo:         $teamsCount equipos")
        println("   - Partidos disputados (ENDED): $totalMatches partidos")
        println("   - Sets simulados:              ~${totalMatches * 4} sets registrados en match_period")
        println("   - Motor de persistencia:       PostgreSQL con Vista Materializada")
        println("   - Calentamiento (Warmup):      $warmupCount peticiones")
        println("   - Muestras por prueba:         $sampleCount iteraciones HTTP")
        println("-----------------------------------------------------------------------------------------")
        println(" METRICAS OBTENIDAS                                LEADERBOARD COMPLETO   HISTORICO (JORNADA 5)")
        println("-----------------------------------------------------------------------------------------")
        println("   Tiempo Mínimo (Min):                       %8.2f ms             %8.2f ms".format(fullStats.min, filteredStats.min))
        println("   Tiempo Promedio (Mean):                    %8.2f ms             %8.2f ms".format(fullStats.avg, filteredStats.avg))
        println("   Mediana (P50):                             %8.2f ms             %8.2f ms".format(fullStats.median, filteredStats.median))
        println("   Percentil 90 (P90):                        %8.2f ms             %8.2f ms".format(fullStats.p90, filteredStats.p90))
        println("   Percentil 95 (P95):                        %8.2f ms             %8.2f ms".format(fullStats.p95, filteredStats.p95))
        println("   Percentil 99 (P99):                        %8.2f ms             %8.2f ms".format(fullStats.p99, filteredStats.p99))
        println("   Tiempo Máximo (Max):                       %8.2f ms             %8.2f ms".format(fullStats.max, filteredStats.max))
        println("   Desviación Estándar (StdDev):              %8.2f ms             %8.2f ms".format(fullStats.stdDev, filteredStats.stdDev))
        println("   Throughput estimado:                       %8.1f req/s          %8.1f req/s".format(fullStats.throughputRps, filteredStats.throughputRps))
        println("=========================================================================================")
        println()
    }
}
