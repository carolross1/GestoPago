DROP TABLE IF EXISTS catalogo_productos;

CREATE TABLE catalogo_productos (
    id_producto INTEGER PRIMARY KEY,
    id_servicio INTEGER NOT NULL,
    id_cat_tipo_servicio INTEGER,
    tipo_front INTEGER NOT NULL,
    servicio VARCHAR(256),
    producto VARCHAR(256),
    precio VARCHAR(20),
    tipo_referencia VARCHAR(5),
    legend TEXT,
    fecha_actualizacion TIMESTAMP NOT NULL
);

CREATE INDEX idx_catalogo_productos_tipo_front ON catalogo_productos (tipo_front);