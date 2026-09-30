# GestoPago - Servicio de Personas y Catalogo de Productos

Backend en Spring Boot que expone un CRUD basico de personas y una
integracion con el proveedor externo GestoPago (PuntoRed) para
autenticacion y consulta del catalogo de productos.

## Tecnologias

- Java 17, Spring Boot 3.3.6
- PostgreSQL (Neon) + Flyway
- Redis (cache del catalogo de productos)
- Feign (cliente HTTP hacia GestoPago)
- JUnit 5 + Mockito

## Configuracion requerida

Completar `src/main/resources/application.properties` con:

- Datos de conexion a PostgreSQL (`spring.datasource.*`)
- Credenciales de GestoPago (`gestopago.auth.*`)
- Datos de Redis (`spring.data.redis.*`)
- Cron de sincronizacion del catalogo (`productos.sync.cron`)

## Como correr el proyecto
./gradlew bootRun

Al arrancar:
1. Se conecta a la base de datos y aplica las migraciones de Flyway.
2. Renueva automaticamente el token de autenticacion de GestoPago.
3. Deja programado el cron diario que sincroniza el catalogo de
   productos (GestoPago -> Redis -> Postgres como respaldo).

## Endpoints principales

- `POST /personas` - crea una persona
- `PUT /personasActualiza` - actualiza una persona por nombre
- `PUT /personasElimina` - elimina una persona por nombre
- `GET /productos` - catalogo de productos agrupado por tipoFront
  (leido de Redis, o de la base de datos si Redis no esta disponible)

## Pruebas

## Notas sobre la integracion con GestoPago

- El endpoint `getProductList` del proveedor tiene un limite de 3
  llamadas por dia, por eso `/productos` nunca lo llama directo: solo
  lee del catalogo ya sincronizado.
- El Bearer token se obtiene y renueva automaticamente, nunca esta
  hardcodeado en el codigo.

## Modulo: Onboarding de clientes personas fisicas

### Tablas
- clientes, domicilios, cuentas, saldos (historial de movimientos), login y sesiones.

### Seguridad
- Todos los campos de texto identificables (nombre, CURP, RFC, contacto,
  domicilio, informacion laboral) se cifran con AES antes de guardarse,
  via EncryptedStringConverter (ver DECISION DE DISENO en esa clase
  sobre por que el cifrado es deterministico: permite mantener busquedas
  exactas y restricciones UNIQUE por CURP/RFC/correo/numero de cuenta).
- ingresoMensual y los montos de saldo se manejan como BigDecimal/NUMERIC,
  nunca como texto cifrado ni como double, para no perder precision en
  operaciones monetarias.
- La contrasena del login NUNCA usa el cifrado reversible: se guarda con
  BCrypt (hash de una via).
- El acceso a /clientes/* y /cuentas/* requiere un JWT valido (header
  Authorization: Bearer) ademas de una sesion activa en base de datos.
  Una sesion se marca inactiva automaticamente tras 5 minutos sin
  actividad (ver seguridad.sesion.inactividad-minutos), obligando a
  iniciar sesion de nuevo.

### Flujo
1. Un ejecutivo registra al cliente: POST /clientes (crea cliente,
   domicilio y su cuenta con saldo inicial de forma automatica).
2. El cliente registra sus credenciales de portal: POST /auth/registro
   (requiere que ya exista como cliente, identificado por RFC).
3. El cliente inicia sesion: POST /auth/login (regresa un JWT).
4. Con ese JWT, el cliente consulta/actualiza su informacion en
   /clientes/** y /cuentas/**.

### Endpoints
- POST /clientes, GET /clientes, GET /clientes/{id}, PUT /clientes/{id}, DELETE /clientes/{id}
- GET /clientes/curp/{curp}, /clientes/rfc/{rfc}, /clientes/correo/{correo}
- GET /clientes/activos, GET /clientes/rango-fechas?desde=...\&hasta=...
- GET /cuentas/{numeroCuenta}, GET /cuentas/{numeroCuenta}/saldo, GET /cuentas/activas
- POST /auth/registro, POST /auth/login

### Pendiente para una siguiente iteracion
- Desbloqueo facial: por eso ingresoMensual/saldo ya se manejan como
  BigDecimal desde ahora, para que un futuro dato biometrico numerico
  se integre sin cambiar el tipo de dato usado en el resto del modulo.
