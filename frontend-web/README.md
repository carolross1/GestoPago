# Plataforma Web de Pagos de Servicios - GestoPago

Frontend Web responsivo e independiente desarrollado para la plataforma de pagos de servicios GestoPago, diseñado para consumir las mismas APIs REST del backend en Spring Boot y compartir exactamente las mismas credenciales de usuario con la aplicación móvil en Flutter.

## Características Principales

1. **Autenticación Compartida con JWT:**
   - Inicio de sesión y registro de contraseña del portal.
   - Acceso con las mismas credenciales en la aplicación móvil y en la web.
   - Persistencia segura de sesión en `localStorage` con interceptores de expiración.

2. **Catálogo Dinámico con Logotipos Oficiales:**
   - Consumo directo de `GET /productos`.
   - Filtros por categorías: Telefonía/Recargas, Luz y Energía (CFE), Agua, Internet/Cable, Streaming y Gobierno.
   - Búsqueda reactiva en tiempo real por proveedor o descripción.
   - Logotipos oficiales en alta definición (CFE, Telcel, Telmex, Movistar, AT&T, Naturgy, Sky, etc.).

3. **Consulta y Validación de Referencias:**
   - Consumo de `POST /transacciones/referencia/validar`.
   - Validación de 10 dígitos numéricos para telefonía y 5-30 caracteres para recibos de servicio.
   - Retroalimentación inmediata con adeudo sugerido.
   - **Bloqueo estricto del pago** si la referencia es inválida o no ha sido verificada.

4. **Confirmación y Procesamiento de Pagos con 2FA:**
   - Resumen previo de la operación.
   - Confirmación mediante código de seguridad de 6 dígitos enviado por correo (`POST /transacciones/2fa`).
   - Procesamiento con GestoPago (`POST /transacciones/pago`).
   - Comprobante digital con **Folio de transacción UPC**, número de autorización, fecha, hora e impresión en PDF.

5. **Historial de Transacciones Oficial:**
   - Consumo de `POST /transacciones/historial`.
   - Consulta de movimientos pertenecientes al usuario autenticado.
   - Visualización de logos de proveedor, fechas, horas, importes, estado `EXITOSA` y folios UPC.
   - Modal con el detalle completo de cada operación.

---

## Cómo Ejecutar el Frontend Web

### Opción 1: Con cualquier navegador web
Simplemente haz doble clic o abre el archivo `index.html` en tu navegador (Google Chrome, Edge, Firefox).

### Opción 2: Con Node.js / npx serve (Recomendado)
Desde una terminal en la carpeta `frontend-web`:

```bash
npx serve . -p 5173
```
Luego abre en tu navegador:
👉 `http://localhost:5173`

---

## Arquitectura de Entregables

```
├── src/               -> Backend REST Spring Boot 3.3.6 (Java 17, PostgreSQL, Flyway)
├── frontend/          -> Aplicación Móvil en Flutter & Dart (Material Design, iOS/Android/Web)
└── frontend-web/      -> Solución Frontend Web Independiente (HTML5, Tailwind CSS, JS ES6)
```
