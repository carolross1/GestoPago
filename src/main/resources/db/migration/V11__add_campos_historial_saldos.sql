-- Extension de columnas de saldos para soportar historial de pagos y referencias
ALTER TABLE saldos ADD COLUMN IF NOT EXISTS referencia VARCHAR(100);
ALTER TABLE saldos ADD COLUMN IF NOT EXISTS folio VARCHAR(50);
ALTER TABLE saldos ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'EXITOSA';
ALTER TABLE saldos ADD COLUMN IF NOT EXISTS id_servicio INTEGER;
ALTER TABLE saldos ADD COLUMN IF NOT EXISTS id_producto INTEGER;
