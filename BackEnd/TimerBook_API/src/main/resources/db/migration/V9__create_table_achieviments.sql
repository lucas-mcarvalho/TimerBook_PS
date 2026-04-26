CREATE TABLE achievements (
                              id BIGSERIAL PRIMARY KEY,
                              key_code VARCHAR(50) UNIQUE NOT NULL,
                              name VARCHAR(100) NOT NULL,
                              description TEXT,
                              icon_url VARCHAR(255)
);
INSERT INTO achievements (key_code, name, description, icon_url) VALUES

('FIRST_LOGIN', 'Primeiro Passo', 'Você iniciou sua jornada no TimerBook!', '🌟'),
('FIRST_BOOK', 'Leitor', 'Terminou seu primeiro livro!', '📚'),
('MARATHON', 'Maratonista', 'Fez uma sessão de leitura de mais de 2 horas.', '🏃‍♂️');