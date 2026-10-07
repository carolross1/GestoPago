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
   con {"correo", "password"} (requiere que ya exista como cliente con ese correo).
3. El cliente inicia sesion: POST /auth/login con {"correo", "password"} (regresa un JWT).
   Respuestas: 400 correo con formato invalido, 404 correo no encontrado,
   404 correo sin contrasena registrada, 401 contrasena incorrecta.
4. Con ese JWT, el cliente consulta/actualiza su informacion en
   /clientes/** y /cuentas/**.

### Endpoints
- POST /clientes, GET /clientes, GET /clientes/{id}, PUT /clientes/{id}, DELETE /clientes/{id}
- GET /clientes/curp/{curp}, /clientes/rfc/{rfc}, /clientes/correo/{correo}
- GET /clientes/activos, GET /clientes/rango-fechas?desde=2026/01/01\&hasta=2026/12/31
- GET /cuentas/{numeroCuenta}, GET /cuentas/{numeroCuenta}/saldo, GET /cuentas/activas
- POST /auth/registro, POST /auth/login

### Pendiente para una siguiente iteracion
- Desbloqueo facial: por eso ingresoMensual/saldo ya se manejan como
  BigDecimal desde ahora, para que un futuro dato biometrico numerico
  se integre sin cambiar el tipo de dato usado en el resto del modulo.

## Formatos y validaciones

- Campos de texto (nombres, nacionalidad, ocupacion, empresa, calle, colonia,
  municipio, estado, pais): solo letras (con acentos y Ñ) y espacios simples.
  Sin numeros ni caracteres especiales.
- Numericos (JSON sin comillas): telefonos (10 digitos), numero exterior/interior
  (1 a 999999) y codigo postal (01000 a 99999; un CP como 06000 viaja como 6000
  y se guarda completo como "06000").
- nacionalidad: codigo ISO de 3 letras del catalogo (ej. MEX). El catalogo
  esta en la tabla catalogo_nacionalidades y se llena desde la API de
  restcountries.com v5 (ver seccion "Catalogo de nacionalidades").
- sexo: H, M u Otros. estadoCivil: SOLTERO, CASADO, DIVORCIADO, VIUDO,
  UNION_LIBRE o SEPARADO.
- Cantidades (ingresoMensual, saldos): se reciben como numero o texto con
  maximo 2 decimales y se regresan SIEMPRE como texto con 2 decimales
  ("15000.00"), para que Swagger/JavaScript no les quiten los ceros.
- Swagger: http://localhost:8080/swagger-ui/index.html. Todas las respuestas
  de error (400, 401, 404, 409) estan documentadas con ejemplos.
- Fechas: formato yyyy/MM/dd (ej. 1999/01/31). La hora se regresa en un campo
  aparte con formato HH:mm:ss (fechaRegistro + horaRegistro, fechaApertura +
  horaApertura, fechaUltimoMovimiento + horaUltimoMovimiento).
- El correo se guarda y se busca en minusculas.

## Catalogo de nacionalidades

1. Crea una API key gratuita en https://restcountries.com/sign-up (plan Free:
   1,000 peticiones al mes) y ponla en `restcountries.api-key` de
   application.properties.
2. Al arrancar, si la tabla `catalogo_nacionalidades` esta vacia, se llena
   sola (~3 peticiones). Despues se re-sincroniza el dia 1 de cada mes
   (`restcountries.sync-cron`). Si la API falla, la app arranca igual.
3. GET /catalogos/nacionalidades (publico): lista codigo, codigoIso2 y nombre
   en espanol, para llenar un combo en el front.
4. POST /catalogos/nacionalidades/sincronizar (requiere token): fuerza la
   sincronizacion.
5. El alta/actualizacion de clientes solo consulta la base de datos, nunca la
   API: el cliente envia `"nacionalidad": "MEX"` y la respuesta incluye
   `nacionalidadNombre: "México"`.
