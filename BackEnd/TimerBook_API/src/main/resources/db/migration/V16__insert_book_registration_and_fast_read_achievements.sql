INSERT INTO achievements (key_code, name, description, icon_url) VALUES
('REGISTERED_BOOKS_3', 'Biblioteca inicial', 'Cadastrou 3 livros na sua biblioteca.', '📚'),
('REGISTERED_BOOKS_10', 'Biblioteca em crescimento', 'Cadastrou 10 livros na sua biblioteca.', '📚'),
('FAST_BOOK_READ_UNDER_1_DAY', 'Leitura relâmpago', 'Finalizou um livro em menos de 1 dia.', '⚡')
ON CONFLICT (key_code) DO NOTHING;
