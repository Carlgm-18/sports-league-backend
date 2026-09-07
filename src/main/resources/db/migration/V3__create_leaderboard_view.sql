-- V3__create_leaderboard_view.sql
CREATE MATERIALIZED VIEW IF NOT EXISTS v_match_team_performance AS
-- Local team perspective
SELECT
    m.id AS match_id,
    p.league_id,
    m.round_id,
    r.sequence_order AS round_sequence_order,
    p.id AS phase_id,
    m.local_team_id AS team_id,
    1 AS played,
    CASE WHEN res.local_total_score > res.visitor_total_score THEN 1 ELSE 0 END AS won,
    CASE WHEN res.local_total_score < res.visitor_total_score THEN 1 ELSE 0 END AS lost,
    CASE WHEN res.local_total_score = res.visitor_total_score THEN 1 ELSE 0 END AS draw,
    COALESCE(pr.local_points,
             CASE
                 WHEN res.local_total_score > res.visitor_total_score THEN 3
                 WHEN res.local_total_score < res.visitor_total_score THEN 0
                 ELSE 1
             END
    ) AS points,
    res.local_total_score AS won_sets,
    res.visitor_total_score AS lost_sets,
    COALESCE(mp.local_points_sum, 0) AS won_points,
    COALESCE(mp.visitor_points_sum, 0) AS lost_points
FROM match m
JOIN round r ON m.round_id = r.id
JOIN phase p ON r.phase_id = p.id
JOIN result res ON m.id = res.match_id
JOIN league l ON p.league_id = l.id
LEFT JOIN punctuation_rule pr ON pr.punctuation_system_id = l.punctuation_system_id
                             AND pr.local_score = res.local_total_score
                             AND pr.visitor_score = res.visitor_total_score
LEFT JOIN (
    -- Sum of period scores per match
    SELECT match_id, SUM(local_score) AS local_points_sum, SUM(visitor_score) AS visitor_points_sum
    FROM match_period
    GROUP BY match_id
) mp ON mp.match_id = m.id
WHERE m.status = 'ENDED'

UNION ALL

-- Visitor team perspective
SELECT
    m.id AS match_id,
    p.league_id,
    m.round_id,
    r.sequence_order AS round_sequence_order,
    p.id AS phase_id,
    m.visitor_team_id AS team_id,
    1 AS played,
    CASE WHEN res.visitor_total_score > res.local_total_score THEN 1 ELSE 0 END AS won,
    CASE WHEN res.visitor_total_score < res.local_total_score THEN 1 ELSE 0 END AS lost,
    CASE WHEN res.visitor_total_score = res.local_total_score THEN 1 ELSE 0 END AS draw,
    COALESCE(pr.visitor_points,
             CASE
                 WHEN res.visitor_total_score > res.local_total_score THEN 3
                 WHEN res.visitor_total_score < res.local_total_score THEN 0
                 ELSE 1
             END
    ) AS points,
    res.visitor_total_score AS won_sets,
    res.local_total_score AS lost_sets,
    COALESCE(mp.visitor_points_sum, 0) AS won_points,
    COALESCE(mp.local_points_sum, 0) AS lost_points
FROM match m
JOIN round r ON m.round_id = r.id
JOIN phase p ON r.phase_id = p.id
JOIN result res ON m.id = res.match_id
JOIN league l ON p.league_id = l.id
LEFT JOIN punctuation_rule pr ON pr.punctuation_system_id = l.punctuation_system_id
                             AND pr.local_score = res.local_total_score
                             AND pr.visitor_score = res.visitor_total_score
LEFT JOIN (
    -- Sum of period scores per match
    SELECT match_id, SUM(local_score) AS local_points_sum, SUM(visitor_score) AS visitor_points_sum
    FROM match_period
    GROUP BY match_id
) mp ON mp.match_id = m.id
WHERE m.status = 'ENDED';

CREATE OR REPLACE FUNCTION refresh_leaderboard()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW v_match_team_performance;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_refresh_leaderboard
    AFTER INSERT OR UPDATE OR DELETE ON result
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_leaderboard();
