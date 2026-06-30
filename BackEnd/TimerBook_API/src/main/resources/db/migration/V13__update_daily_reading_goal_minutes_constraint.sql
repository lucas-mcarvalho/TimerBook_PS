ALTER TABLE users
DROP CONSTRAINT IF EXISTS chk_users_daily_reading_goal_minutes;

ALTER TABLE users
ADD CONSTRAINT chk_users_daily_reading_goal_minutes
CHECK (daily_reading_goal_minutes IN (5, 10, 15, 30, 60));
