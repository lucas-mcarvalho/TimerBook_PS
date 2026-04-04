CREATE TABLE IF NOT EXISTS books (
                                     id          BIGSERIAL PRIMARY KEY,
                                     name        VARCHAR(255),
    description TEXT,
    cover_url   VARCHAR(500),
    data_path   VARCHAR(500)
    );
