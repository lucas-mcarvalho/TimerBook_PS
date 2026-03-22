CREATE TABLE tb_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cover_url VARCHAR(512),
    data_path VARCHAR(512)
);