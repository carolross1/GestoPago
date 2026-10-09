CREATE TABLE saldos (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    monto NUMERIC(14,2) NOT NULL,
    saldo_resultante NUMERIC(14,2) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    CONSTRAINT fk_saldos_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES cuentas (id) ON DELETE CASCADE,
    CONSTRAINT ck_saldos_no_negativo CHECK (saldo_resultante >= 0)
);

CREATE INDEX idx_saldos_cuenta_fecha ON saldos (cuenta_id, fecha DESC);
