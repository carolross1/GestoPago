CREATE TABLE login (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    password_hash TEXT NOT NULL,
    fecha_registro TIMESTAMP NOT NULL,
    CONSTRAINT uk_login_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_login_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE CASCADE
);

CREATE TABLE sesiones (
    id BIGSERIAL PRIMARY KEY,
    login_id BIGINT NOT NULL,
    token TEXT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL,
    ultima_actividad TIMESTAMP NOT NULL,
    CONSTRAINT fk_sesiones_login FOREIGN KEY (login_id)
        REFERENCES login (id) ON DELETE CASCADE
);

CREATE INDEX idx_sesiones_activa ON sesiones (activa);
