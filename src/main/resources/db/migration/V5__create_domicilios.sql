CREATE TABLE domicilios (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    calle TEXT NOT NULL,
    numero_exterior TEXT NOT NULL,
    numero_interior TEXT,
    colonia TEXT NOT NULL,
    municipio TEXT NOT NULL,
    estado TEXT NOT NULL,
    codigo_postal TEXT NOT NULL,
    pais TEXT NOT NULL,
    CONSTRAINT uk_domicilios_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE CASCADE
);
