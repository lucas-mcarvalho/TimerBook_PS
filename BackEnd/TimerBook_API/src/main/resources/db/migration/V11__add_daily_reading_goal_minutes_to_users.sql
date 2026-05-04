ALTER TABLE users
ADD COLUMN IF NOT EXISTS daily_reading_goal_minutes INTEGER;

UPDATE users
SET daily_reading_goal_minutes = 10
WHERE daily_reading_goal_minutes IS NULL;

ALTER TABLE users
ALTER COLUMN daily_reading_goal_minutes SET DEFAULT 10;

ALTER TABLE users
ALTER COLUMN daily_reading_goal_minutes SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_users_daily_reading_goal_minutes'
    ) THEN
        ALTER TABLE users
        ADD CONSTRAINT chk_users_daily_reading_goal_minutes
        CHECK (daily_reading_goal_minutes IN (10, 20, 30));
    END IF;
END $$;
