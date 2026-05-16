INSERT INTO sport (sport_name)
VALUES ('VOLLEYBALL'),
       ('FOOTBALL'),
       ('PADEL')
ON CONFLICT (sport_name) DO NOTHING;

INSERT INTO participation_role (role_name)
VALUES ('ADMIN'),
       ('REFEREE'),
       ('CAPTAIN'),
       ('PLAYER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO punctuation_system (sport_id, name)
VALUES (1, 'OFFICIAL VOLLEYBALL SYSTEM');

INSERT INTO punctuation_rule (punctuation_system_id, local_score, visitor_score, local_points, visitor_points)
VALUES (1, 3, 0, 3, 0),
       (1, 3, 1, 3, 0),
       (1, 3, 2, 2, 1),
       (1, 2, 3, 1, 2),
       (1, 1, 3, 0, 3),
       (1, 0, 3, 0, 3);

INSERT INTO league_configuration(sport_id, name, category, min_team_female_integrants, min_team_members,
                                 max_team_members, round_duration)
VALUES (1, 'NORMATIVA VOLLEYBALL UIB 2025-2026', 'MIXT', 2, 6, 14, 1);