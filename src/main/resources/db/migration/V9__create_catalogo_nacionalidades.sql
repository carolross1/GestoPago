-- Catalogo de nacionalidades (paises) que se llena desde la API de
-- restcountries.com. Son datos publicos, por eso NO se cifran.
-- El cliente guarda el "codigo" (ISO 3166-1 alfa-3, ej. MEX).
CREATE TABLE catalogo_nacionalidades (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(3) NOT NULL,
    codigo_iso2 VARCHAR(2),
    nombre VARCHAR(150) NOT NULL,
    nombre_ingles VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP NOT NULL,
    CONSTRAINT uk_catalogo_nacionalidades_codigo UNIQUE (codigo)
);

CREATE INDEX idx_catalogo_nacionalidades_nombre ON catalogo_nacionalidades (nombre);
