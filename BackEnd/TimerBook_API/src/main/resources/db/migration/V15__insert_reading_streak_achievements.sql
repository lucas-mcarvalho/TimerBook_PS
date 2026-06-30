INSERT INTO achievements (key_code, name, description, icon_url) VALUES
('READING_STREAK_1', 'Sequência de 1 dia', 'Leu por 1 dia seguido.', '🔥'),
('READING_STREAK_10', 'Sequência de 10 dias', 'Leu por 10 dias seguidos.', '🔥'),
('READING_STREAK_15', 'Sequência de 15 dias', 'Leu por 15 dias seguidos.', '🔥'),
('READING_STREAK_30', 'Sequência de 30 dias', 'Leu por 30 dias seguidos.', '🔥')
ON CONFLICT (key_code) DO NOTHING;
