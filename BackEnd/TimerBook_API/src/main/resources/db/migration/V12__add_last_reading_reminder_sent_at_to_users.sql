ALTER TABLE users
ADD COLUMN IF NOT EXISTS last_reading_reminder_sent_at TIMESTAMP;