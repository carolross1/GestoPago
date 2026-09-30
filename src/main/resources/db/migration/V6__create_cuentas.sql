CREATE TABLE cuentas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    numero_cuenta TEXT NOT NULL,
    estatus VARCHAR(20) NOT NULL,
    fecha_apertura TIMESTAMP NOT NULL,
    CONSTRAINT uk_cuentas_numero UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE CASCADE,
    CONSTRAINT ck_cuentas_estatus CHECK (estatus IN ('ACTIVA', 'INACTIVA'))
);

CREATE INDEX idx_cuentas_cliente ON cuentas (cliente_id);
CREATE INDEX idx_cuentas_estatus ON cuentas (estatus);
