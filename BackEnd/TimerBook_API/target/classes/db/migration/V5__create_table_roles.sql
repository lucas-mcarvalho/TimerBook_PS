create table tb_role (
    id BIGSERIAL PRIMARY KEY,
    authority VARCHAR(255) NOT NULL UNIQUE
)