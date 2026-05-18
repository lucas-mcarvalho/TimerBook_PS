-- Add subscription_plan column to support different goal rules for free and paid users
ALTER TABLE users
ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50) DEFAULT 'FREE';

-- Remove the fixed values constraint
ALTER TABLE users
DROP CONSTRAINT IF EXISTS chk_users_daily_reading_goal_minutes;

-- Add a new constraint that only validates positive values
ALTER TABLE users
ADD CONSTRAINT chk_users_daily_reading_goal_minutes_positive
CHECK (daily_reading_goal_minutes > 0);

-- Add constraint for valid subscription plans
ALTER TABLE users
ADD CONSTRAINT chk_users_subscription_plan
CHECK (subscription_plan IN ('FREE', 'PAID'));
