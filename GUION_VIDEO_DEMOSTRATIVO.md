# Guion Demostrativo (2 a 3 Minutos)
## Plataforma Omnicanal de Pagos de Servicios - GestoPago

---

### Ficha Técnica
- **Duración estimada:** 2 minutos y 30 segundos.
- **Participantes:** Presentador / Cuentahabiente.
- **Herramientas a mostrar:**
  - Aplicación Móvil Flutter (`http://localhost:3000` / Dispositivo Android).
  - Plataforma Web SPA (`http://localhost:5173`).
  - Correo electrónico de pruebas (`carolrios347@gmail.com`).
  - Consola y Logs del Backend Spring Boot (`:8080`).

---

### Bloque 1: Introducción y Arquitectura (0:00 - 0:30)
- **Visual:** Pantalla dividida con la App Móvil (Flutter) a la izquierda y la Web SPA a la derecha.
- **Voz en off:**
  > *"Bienvenidos a la presentación de la Plataforma de Pagos de Servicios GestoPago. Se trata de una solución integral y omnicanal desarrollada con backend en Spring Boot, base de datos PostgreSQL en la nube, y dos interfaces cliente de última generación: una aplicación móvil en Flutter con diseño Apple Wallet & Glassmorphism, y un portal Web SPA responsivo construido con Tailwind CSS y JavaScript modular.*
  > *Ambas plataformas comparten una misma cuenta bancaria y garantizan seguridad de grado financiero con autenticación de doble factor 2FA por correo electrónico."*

---

### Bloque 2: Inicio de Sesión y Dashboard Móvil (0:30 - 1:00)
- **Visual:** Primer plano de la app móvil en Flutter (`localhost:3000`).
- **Acción:**
  1. El presentador inicia sesión con el usuario `carolrios347@gmail.com`.
  2. Muestra la pantalla principal con la **Tarjeta Digital 3D Holográfica** en tonos violeta y lila.
  3. Toca el icono de ojo para mostrar y ocultar el saldo disponible de la cuenta.
  4. Muestra la **Hoja Deslizable (Curved Bottom Sheet)** deslizando hacia arriba para ver el carrusel Stories de servicios frecuentes y los últimos movimientos.
  5. Señala la nueva **Barra de Navegación Flotante (Floating Glass Dock)** en la parte inferior.
- **Voz en off:**
  > *"Al ingresar a la aplicación móvil, observamos una experiencia visual de alto nivel. La tarjeta bancaria holográfica nos permite consultar o proteger nuestro saldo con un toque. En la parte inferior, la hoja deslizable organiza los servicios más utilizados en formato de burbujas interactivas con sus logotipos oficiales, mientras que el Floating Glass Dock nos brinda una navegación fluida e instantánea."*

---

### Bloque 3: Catálogo y Flujo de Pago con 2FA (1:00 - 1:50)
- **Visual:** Navegación al **Catálogo de Servicios** desde la barra flotante o desde el carrusel Stories.
- **Acción:**
  1. En el catálogo en cuadrícula de 2 columnas, el presentador selecciona un servicio (por ejemplo, **CFE Suministro Básico** o **Telcel Recargas**).
  2. Ingresa la referencia del recibo o número telefónico.
  3. Presiona **Continuar al Pago**.
  4. El sistema valida la referencia contra el simulador de GestoPago y solicita el código **2FA**.
  5. El presentador abre su bandeja de entrada en `carolrios347@gmail.com`, donde ha llegado el correo con el código OTP de 6 dígitos.
  6. Ingresa el código en el modal de la app y presiona **Confirmar Pago**.
  7. La pantalla muestra la animación de éxito y el **Comprobante Digital Troquelado** con el Folio UPC, fecha, hora y desglose.
- **Voz en off:**
  > *"Al seleccionar el servicio, el sistema valida la referencia en tiempo real. Por seguridad, se genera un código dinámico de doble factor que llega al correo registrado del titular. Al ingresar el código, el débito se realiza en la cuenta bancaria y se emite de inmediato el comprobante digital bancario con su folio único de autorización."*

---

### Bloque 4: Sincronización Omnicanal en la Plataforma Web (1:50 - 2:20)
- **Visual:** Cambio a la pantalla del Portal Web SPA (`http://localhost:5173`).
- **Acción:**
  1. Inicia sesión con la misma cuenta.
  2. El saldo de la cuenta se actualiza automáticamente reflejando el pago recién hecho en la app móvil.
  3. En la sección de **Historial de Transacciones**, el pago efectuado en la móvil aparece registrado de forma inmediata.
  4. Abre el recibo digital troquelado en la web y presiona el botón **Imprimir Comprobante (PDF)**.
- **Voz en off:**
  > *"Pasamos a la plataforma web. Dado que la arquitectura es compartida, el saldo se encuentra perfectamente sincronizado y el pago que realizamos en el móvil aparece de inmediato en el historial web. Podemos visualizar el comprobante con estilo ticket bancario y exportarlo a PDF en cualquier momento."*

---

### Bloque 5: Cierre (2:20 - 2:30)
- **Visual:** Ambas plataformas sincronizadas en pantalla completa.
- **Voz en off:**
  > *"Con esto demostramos el cumplimiento de los requerimientos de arquitectura, integración con la pasarela GestoPago, diseño de interfaces modernas y seguridad 2FA. Muchas gracias."*
