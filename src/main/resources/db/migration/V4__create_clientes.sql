CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    segundo_nombre TEXT,
    apellido_paterno TEXT NOT NULL,
    apellido_materno TEXT NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    curp TEXT NOT NULL,
    rfc TEXT NOT NULL,
    sexo TEXT NOT NULL,
    nacionalidad TEXT NOT NULL,
    estado_civil TEXT NOT NULL,
    correo TEXT NOT NULL,
    telefono_movil TEXT NOT NULL,
    telefono_alternativo TEXT,
    ocupacion TEXT NOT NULL,
    empresa TEXT NOT NULL,
    ingreso_mensual NUMERIC(14,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL,
    CONSTRAINT uk_clientes_curp UNIQUE (curp),
    CONSTRAINT uk_clientes_rfc UNIQUE (rfc),
    CONSTRAINT uk_clientes_correo UNIQUE (correo),
    CONSTRAINT ck_clientes_ingreso_mensual CHECK (ingreso_mensual > 0)
);

CREATE INDEX idx_clientes_activo ON clientes (activo);
CREATE INDEX idx_clientes_fecha_registro ON clientes (fecha_registro);
