UPDATE login 
SET password_hash = (SELECT password_hash FROM login WHERE cliente_id = 3)
WHERE cliente_id = 5;

UPDATE saldos 
SET saldo_resultante = 1000.00, monto = 1000.00 
WHERE cuenta_id = (SELECT id FROM cuentas WHERE cliente_id = 5);