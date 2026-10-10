# Diagrama Entidad-Relacion - Modulo de Clientes

```mermaid
erDiagram
    CLIENTES ||--|| DOMICILIOS : "tiene"
    CLIENTES ||--|| CUENTAS : "es titular de"
    CLIENTES ||--o| LOGIN : "puede registrar"
    CUENTAS ||--o{ SALDOS : "acumula historial"
    LOGIN ||--o{ SESIONES : "genera"

    CLIENTES {
        bigint id PK
        text nombre "cifrado"
        text segundo_nombre "cifrado, opcional"
        text apellido_paterno "cifrado"
        text apellido_materno "cifrado"
        date fecha_nacimiento
        text curp "cifrado, UNIQUE"
        text rfc "cifrado, UNIQUE"
        text sexo "cifrado"
        text nacionalidad "cifrado"
        text estado_civil "cifrado"
        text correo "cifrado, UNIQUE"
        text telefono_movil "cifrado"
        text telefono_alternativo "cifrado, opcional"
        text ocupacion "cifrado"
        text empresa "cifrado"
        numeric ingreso_mensual "BigDecimal, sin cifrar"
        boolean activo
        timestamp fecha_registro
        timestamp fecha_actualizacion
    }

    DOMICILIOS {
        bigint id PK
        bigint cliente_id FK "UNIQUE"
        text calle "cifrado"
        text numero_exterior "cifrado"
        text numero_interior "cifrado, opcional"
        text colonia "cifrado"
        text municipio "cifrado"
        text estado "cifrado"
        text codigo_postal "cifrado"
        text pais "cifrado"
    }

    CUENTAS {
        bigint id PK
        bigint cliente_id FK
        text numero_cuenta "cifrado, UNIQUE"
        varchar estatus "ACTIVA / INACTIVA"
        timestamp fecha_apertura
    }

    SALDOS {
        bigint id PK
        bigint cuenta_id FK
        varchar tipo_movimiento "APERTURA / DEPOSITO / RETIRO / AJUSTE"
        numeric monto "BigDecimal"
        numeric saldo_resultante "BigDecimal, CHECK >= 0"
        timestamp fecha
    }

    LOGIN {
        bigint id PK
        bigint cliente_id FK "UNIQUE"
        text password_hash "BCrypt, no reversible"
        timestamp fecha_registro
    }

    SESIONES {
        bigint id PK
        bigint login_id FK
        text token "JWT"
        boolean activa
        timestamp fecha_creacion
        timestamp ultima_actividad
    }
```

## Notas del diseno

- **CLIENTES 1---1 DOMICILIOS**: un domicilio por cliente (`UNIQUE` en `cliente_id`).
- **CLIENTES 1---1 CUENTAS**: cada cliente tiene exactamente una cuenta, creada automaticamente al registrarse.
- **CUENTAS 1---N SALDOS**: cada movimiento de saldo genera una fila nueva; el saldo actual es el de `fecha` mas reciente para esa cuenta.
- **CLIENTES 1---0/1 LOGIN**: un cliente puede o no tener credenciales de portal registradas (se crean aparte, via `POST /auth/registro`).
- **LOGIN 1---N SESIONES**: cada inicio de sesion exitoso genera una fila nueva; la bandera `activa` se apaga sola tras 5 minutos sin actividad.
- Los campos marcados "cifrado" usan `AES/ECB` deterministico (ver `EncryptedStringConverter`), por eso siguen pudiendo llevar `UNIQUE`/busquedas exactas a pesar de estar cifrados.
- Los campos monetarios (`ingreso_mensual`, `monto`, `saldo_resultante`) son `NUMERIC` (BigDecimal en Java), nunca texto ni `double`.
