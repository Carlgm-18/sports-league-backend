-- V1__init_schema.sql

-- ==========================================
-- 1. CONFIGURACIÓN MAESTRA Y USUARIOS
-- ==========================================

CREATE TABLE IF NOT EXISTS app_user
(
    id                SERIAL PRIMARY KEY,
    first_name        VARCHAR(50)         NOT NULL,
    last_name         VARCHAR(50)         NOT NULL,
    email             VARCHAR(100) UNIQUE NOT NULL,
    category          VARCHAR(50)         NOT NULL, -- MALE, FEMALE
    password_hash     VARCHAR(255)        NOT NULL,
    profile_image_url VARCHAR(255),
    created_at        TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sport
(
    id         SERIAL PRIMARY KEY,
    sport_name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS referee_license
(
    id          SERIAL PRIMARY KEY,
    app_user_id INT          NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    sport_id    INT          NOT NULL REFERENCES sport (id) ON DELETE CASCADE,
    license     VARCHAR(100) NOT NULL,
    upload_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS league_configuration
(
    id                         SERIAL PRIMARY KEY,
    sport_id                   INT          NOT NULL REFERENCES sport (id),
    name                       VARCHAR(100) NOT NULL,
    category                   VARCHAR(50)  NOT NULL, -- MALE, FEMALE, MIXT
    min_team_female_integrants INT,
    min_team_members           INT          NOT NULL,
    max_team_members           INT          NOT NULL,
    round_duration             INT          NOT NULL
);

-- ==========================================
-- 2. CORE: LIGAS, EQUIPOS Y PARTICIPACIONES
-- ==========================================

CREATE TABLE IF NOT EXISTS punctuation_system
(
    id             SERIAL PRIMARY KEY,
    sport_id       INT          NOT NULL REFERENCES sport (id) ON DELETE CASCADE,
    name           VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS punctuation_rule
(
    id                      SERIAL PRIMARY KEY,
    punctuation_system_id   INT NOT NULL REFERENCES punctuation_system (id) ON DELETE CASCADE
    local_score             INT NOT NULL,
    visitor_score           INT NOT NULL,
    local_points            INT NOT NULL,
    visitor_points          INT NOT NULL
);

CREATE TABLE IF NOT EXISTS league
(
    id                      SERIAL PRIMARY KEY,
    configuration_id        INT          NOT NULL REFERENCES league_configuration (id),
    punctuation_system_id   INT NOT NULL REFERENCES punctuation_system (id) ON DELETE RESTRICT,
    name                    VARCHAR(100) NOT NULL,
    description             TEXT,
    icon_image_url          VARCHAR(255),
    banner_image_url        VARCHAR(255),
    location_url            VARCHAR(255),
    start_date              DATE         NOT NULL,
    end_date                DATE         NOT NULL,
    max_inscription_date    DATE,
    status                  VARCHAR(50)  NOT NULL, -- TEAM_ASSEMBLE, MATCH_MAKING, IN_PROGRESS, ENDED
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP
);

CREATE TABLE IF NOT EXISTS team
(
    id              SERIAL PRIMARY KEY,
    league_id       INT          NOT NULL REFERENCES league (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    initials        VARCHAR(10)  NOT NULL,
    description     TEXT,
    motto           VARCHAR(255),
    primary_color   VARCHAR(7)   DEFAULT '#FFFFFF',
    secondary_color VARCHAR(7)   DEFAULT '#000000',
    icon_image_url  VARCHAR(255),
    deleted_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS participation_role
(
    id        SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE -- PLAYER, CAPTAIN, REFEREE, ADMIN
);

CREATE TABLE IF NOT EXISTS participant
(
    id          SERIAL PRIMARY KEY,
    app_user_id INT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    league_id   INT       NOT NULL REFERENCES league (id) ON DELETE CASCADE,
    team_id     INT       REFERENCES team (id) ON DELETE SET NULL,
    dorsal      INT,
    join_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (app_user_id, league_id)
);

CREATE TABLE IF NOT EXISTS participant_role
(
    id                  SERIAL PRIMARY KEY,
    participant_id    INT NOT NULL REFERENCES participant (id) ON DELETE CASCADE,
    participation_role_id INT NOT NULL REFERENCES participation_role (id) ON DELETE CASCADE,
    UNIQUE (participant_id, participation_role_id)
);

-- ==========================================
-- 3. ESTRUCTURA DE JUEGO (FASES Y RONDAS)
-- ==========================================

CREATE TABLE IF NOT EXISTS phase
(
    id             SERIAL PRIMARY KEY,
    league_id      INT          NOT NULL REFERENCES league (id) ON DELETE CASCADE,
    name           VARCHAR(100) NOT NULL,
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    sequence_order INT          NOT NULL,
    stages_number  INT,
    phase_type     VARCHAR(50)  NOT NULL -- CLASSIFICATION, TOURNAMENT
);

CREATE TABLE IF NOT EXISTS classification_group
(
    id          SERIAL PRIMARY KEY,
    phase_id    INT NOT NULL REFERENCES phase (id) ON DELETE CASCADE,
    top_winners INT NOT NULL
);

CREATE TABLE IF NOT EXISTS classification_group_team
(
    id                      SERIAL PRIMARY KEY,
    classification_group_id INT NOT NULL REFERENCES classification_group (id) ON DELETE CASCADE,
    team_id                 INT NOT NULL REFERENCES team (id) ON DELETE CASCADE,
    UNIQUE (classification_group_id, team_id)
);

CREATE TABLE IF NOT EXISTS round
(
    id        SERIAL PRIMARY KEY,
    phase_id  INT  NOT NULL REFERENCES phase (id) ON DELETE CASCADE,
    first_day DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS datetime_slot
(
    id        SERIAL PRIMARY KEY,
    round_id  INT REFERENCES round (id) ON DELETE CASCADE,
    date_time TIMESTAMP NOT NULL,
    duration  INT       NOT NULL
);

-- ==========================================
-- 4. PARTIDOS Y RESULTADOS
-- ==========================================

CREATE TABLE IF NOT EXISTS match
(
    id                SERIAL PRIMARY KEY,
    round_id          INT         NOT NULL REFERENCES round (id) ON DELETE CASCADE,
    datetime_slot_id  INT         REFERENCES datetime_slot (id) ON DELETE SET NULL,
    local_team_id     INT         REFERENCES team (id) ON DELETE SET NULL,
    visitor_team_id   INT         REFERENCES team (id) ON DELETE SET NULL,
    first_referee_id  INT REFERENCES participant (id),
    second_referee_id INT REFERENCES participant (id),
    status            VARCHAR(50) NOT NULL -- NOT_SCHEDULED, SCHEDULED, IN_GAME, ENDED
);

CREATE TABLE IF NOT EXISTS tournament_slot
(
    id       SERIAL PRIMARY KEY,
    phase_id INT NOT NULL REFERENCES phase (id) ON DELETE CASCADE,
    match_id INT NOT NULL REFERENCES match (id) ON DELETE CASCADE,
    index_order INT NOT NULL UNIQUE
);

-- CREATE TABLE IF NOT EXISTS result
-- (
--     id                  SERIAL PRIMARY KEY,
--     match_id            INT NOT NULL UNIQUE REFERENCES match (id) ON DELETE CASCADE,
--     local_total_score   INT NOT NULL DEFAULT 0,
--     visitor_total_score INT NOT NULL DEFAULT 0,
--     record_url          VARCHAR(255)
-- );

-- CREATE TABLE IF NOT EXISTS observation
-- (
--     id        SERIAL PRIMARY KEY,
--     result_id INT  NOT NULL REFERENCES result (id) ON DELETE CASCADE,
--     text      TEXT NOT NULL
-- );

CREATE TABLE IF NOT EXISTS sign
(
    id             SERIAL PRIMARY KEY,
    sign_image_url VARCHAR(255) NOT NULL,
    upload_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    app_user_id    INT          NOT NULL REFERENCES app_user (id) ON DELETE CASCADE
);

-- CREATE TABLE IF NOT EXISTS match_sign
-- (
--     id        SERIAL PRIMARY KEY,
--     result_id  INT         NOT NULL REFERENCES result (id) ON DELETE CASCADE,
--     sign_id   INT         NOT NULL REFERENCES sign (id) ON DELETE CASCADE,
--     moment    VARCHAR(50) NOT NULL, -- PRE_MATCH, POST_MATCH
--     role      VARCHAR(50) NOT NULL, -- FIRST_REFEREE, LOCAL_CAPTAIN...
--     signed_at TIMESTAMP   NOT NULL
-- );

-- ==========================================
-- 5. EVENTOS DENTRO DEL PARTIDO
-- ==========================================

-- CREATE TABLE IF NOT EXISTS match_period
-- (
--     id               SERIAL PRIMARY KEY,
--     match_id         INT         NOT NULL REFERENCES match (id) ON DELETE CASCADE,
--     period_number    INT         NOT NULL,
--     local_score      INT         NOT NULL DEFAULT 0,
--     visitor_score    INT         NOT NULL DEFAULT 0,
--     period_type_name VARCHAR(50) NOT NULL
-- );
--
-- CREATE TABLE IF NOT EXISTS match_event
-- (
--     id               SERIAL PRIMARY KEY,
--     match_period_id  INT  NOT NULL REFERENCES match_period (id) ON DELETE CASCADE,
--     team_id          INT REFERENCES team (id) ON DELETE CASCADE,
--     happened_at_time TIME NOT NULL, -- Formato HH:MM:SS
--     at_local_score   INT  NOT NULL,
--     at_visitor_score INT  NOT NULL
-- );
--
-- CREATE TABLE IF NOT EXISTS substitution
-- (
--     id                 SERIAL PRIMARY KEY,
--     match_id           INT NOT NULL REFERENCES match (id) ON DELETE CASCADE,
--     outgoing_player_id INT NOT NULL REFERENCES participant (id),
--     incoming_player_id INT NOT NULL REFERENCES participant (id)
-- );

-- ==========================================
-- 6. SOLICITUDES, SANCIONES E INCIDENCIAS
-- ==========================================

-- CREATE TABLE IF NOT EXISTS sanction
-- (
--     id                 SERIAL PRIMARY KEY,
--     participant_id   INT          NOT NULL REFERENCES participant (id) ON DELETE CASCADE,
--     reason             TEXT         NOT NULL,
--     sanction_type_name VARCHAR(100) NOT NULL
-- );

CREATE TABLE IF NOT EXISTS incidence
(
    id               SERIAL PRIMARY KEY,
    league_id        INT  NOT NULL REFERENCES league (id) ON DELETE CASCADE,
    participant_id INT  NOT NULL REFERENCES participant (id) ON DELETE CASCADE,
    description      TEXT NOT NULL,
    resolution       TEXT
);

CREATE TABLE IF NOT EXISTS team_create_request
(
    id               SERIAL PRIMARY KEY,
    league_id        INT         NOT NULL REFERENCES league (id) ON DELETE CASCADE,
    team_id          INT         NOT NULL REFERENCES team (id) ON DELETE CASCADE,
    participant_id INT         NOT NULL REFERENCES participant (id),
    status           VARCHAR(50) NOT NULL, -- PENDING, REJECTED, APPROVED, CANCELED
    rejection_reason TEXT,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS team_join_request
(
    id               SERIAL PRIMARY KEY,
    team_id          INT         NOT NULL REFERENCES team (id) ON DELETE CASCADE,
    participant_id INT         NOT NULL REFERENCES participant (id) ON DELETE CASCADE,
    way              VARCHAR(50) NOT NULL, -- APPLIANCE, INVITATION
    status           VARCHAR(50) NOT NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proposal
(
    id               SERIAL PRIMARY KEY,
    match_id         INT         NOT NULL REFERENCES match (id) ON DELETE CASCADE,
    datetime_slot_id INT         NOT NULL REFERENCES datetime_slot (id),
    status           VARCHAR(50) NOT NULL, -- PENDING, REJECTED, APPROVED
    proposed_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP
);