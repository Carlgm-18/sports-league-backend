INSERT INTO sport (sport_name)
VALUES ('VOLLEYBALL'), ('FOOTBALL'), ('PADEL')
ON CONFLICT (sport_name) DO NOTHING;

INSERT INTO participation_role (role_name)
VALUES ('ADMIN'), ('REFEREE'), ('CAPTAIN'), ('PLAYER')
ON CONFLICT (role_name) DO NOTHING;

-- TODO: Add punctuation system and rules inserst
