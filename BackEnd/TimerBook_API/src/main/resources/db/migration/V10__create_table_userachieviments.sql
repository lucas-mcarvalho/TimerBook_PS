CREATE TABLE user_achievements (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
                                   unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE(user_id, achievement_id)
);