CREATE DATABASE IF NOT EXISTS timerbook;
USE timerbook;

CREATE TABLE IF NOT EXISTS books (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    cover_url   VARCHAR(500),
    data_path   VARCHAR(500)
);