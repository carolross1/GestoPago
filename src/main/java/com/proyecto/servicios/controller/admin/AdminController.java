package com.proyecto.servicios.controller.admin;

import com.proyecto.servicios.model.cliente.AbonoRequest;
import com.proyecto.servicios.model.cliente.AbonoResponse;
import com.proyecto.servicios.model.cliente.ClienteFiltroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.security.JwtUtil;
import com.proyecto.servicios.service.cliente.ClienteService;
import com.proyecto.servicios.service.cliente.CuentaService;
import com.proyecto.servicios.util.Respuestas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints exclusivos del Panel Administrativo (separado del portal de clientes).
 * Protegido por AdminAuthFilter con JWT de rol=ADMIN.
 */
@Slf4j
@Tag(name = "07. Panel Administrativo", description = "Autenticación administrativa y abonos de saldo a clientes")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final JwtUtil jwtUtil;
    private final ClienteService clienteService;
    private final CuentaService cuentaService;

    @Value("${admin.correo:admin@gestopago.com}")
    private String adminCorreo;

    @Value("${admin.password:GestoAdmin2026!}")
    private String adminPassword;

    public AdminController(JwtUtil jwtUtil, ClienteService clienteService, CuentaService cuentaService) {
        this.jwtUtil = jwtUtil;
        this.clienteService = clienteService;
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Iniciar sesión en el Panel Administrativo")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<RespuestaApi<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String correo = body != null && body.get("correo") != null ? body.get("correo").trim().toLowerCase() : "";
        String password = body != null && body.get("password") != null ? body.get("password") : "";

        boolean correoOk = adminCorreo != null && adminCorreo.trim().equalsIgnoreCase(correo);
        boolean passOk = adminPassword != null && MessageDigest.isEqual(
                adminPassword.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8)
        );

        if (!correoOk || !passOk) {
            log.warn("Intento fallido de acceso administrativo para correo={}", correo);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    RespuestaApi.error(401, "Credenciales de administrador incorrectas",
                            "/api/admin/login", Collections.emptyList())
            );
        }

        String token = jwtUtil.generarTokenAdmin(correo);
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("token", token);
        datos.put("correo", correo);
        datos.put("rol", "ADMIN");
        datos.put("expiraEnMs", jwtUtil.getExpiracionMs());

        log.info("Inicio de sesión administrativo exitoso para {}", correo);
        return Respuestas.ok("Bienvenido al Panel Administrativo", datos);
    }

    @Operation(summary = "Validar si el token de administrador sigue vigente")
    @GetMapping("/sesion")
    public ResponseEntity<RespuestaApi<Map<String, Object>>> verificarSesion() {
        return Respuestas.ok("Sesión administrativa activa", Map.of("activa", true, "rol", "ADMIN"));
    }

    @Operation(summary = "Listar todos los clientes con sus cuentas y saldos actuales")
    @GetMapping("/cuentas")
    public ResponseEntity<RespuestaApi<List<Map<String, Object>>>> listarCuentas() {
        ClienteFiltroRequest filtroCli = new ClienteFiltroRequest();
        filtroCli.setPagina(0);
        filtroCli.setTamano(200);
        PaginaResponse<ClienteResponse> pagClientes = clienteService.buscar(filtroCli);

        Map<Long, ClienteResponse> mapaClientes = new HashMap<>();
        if (pagClientes != null && pagClientes.getContenido() != null) {
            for (ClienteResponse c : pagClientes.getContenido()) {
                if (c.getId() != null) {
                    mapaClientes.put(c.getId(), c);
                }
            }
        }

        CuentaFiltroRequest filtroCta = new CuentaFiltroRequest();
        filtroCta.setPagina(0);
        filtroCta.setTamano(200);
        PaginaResponse<CuentaResponse> pagCuentas = cuentaService.buscar(filtroCta);

        List<Map<String, Object>> resultado = new ArrayList<>();
        if (pagCuentas != null && pagCuentas.getContenido() != null) {
            for (CuentaResponse cta : pagCuentas.getContenido()) {
                ClienteResponse cli = mapaClientes.get(cta.getClienteId());
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("clienteId", cta.getClienteId());
                fila.put("numeroCuenta", cta.getNumeroCuenta());
                fila.put("saldoActual", cta.getSaldoActual());
                fila.put("estatus", cta.getEstatus());
                if (cli != null) {
                    StringBuilder nombre = new StringBuilder();
                    if (cli.getNombre() != null) nombre.append(cli.getNombre());
                    if (cli.getSegundoNombre() != null && !cli.getSegundoNombre().isBlank()) {
                        nombre.append(" ").append(cli.getSegundoNombre());
                    }
                    if (cli.getApellidoPaterno() != null) nombre.append(" ").append(cli.getApellidoPaterno());
                    if (cli.getApellidoMaterno() != null) nombre.append(" ").append(cli.getApellidoMaterno());
                    fila.put("nombreCompleto", nombre.toString().trim());
                    fila.put("correo", cli.getCorreo() != null ? cli.getCorreo() : "");
                } else {
                    fila.put("nombreCompleto", "Cliente #" + cta.getClienteId());
                    fila.put("correo", "");
                }
                resultado.add(fila);
            }
        }

        return Respuestas.ok("Se encontraron " + resultado.size() + " cuenta(s)", resultado);
    }

    @Operation(summary = "Abonar saldo a la cuenta de un cliente (solo administradores)")
    @PostMapping("/abono")
    public ResponseEntity<RespuestaApi<AbonoResponse>> abonar(@Valid @RequestBody AbonoRequest request) {
        // Aseguramos que el concepto por defecto ("ABONO") respete la restricción de base de datos
        request.setConcepto(null);
        AbonoResponse resp = cuentaService.abonar(request);
        return Respuestas.ok("Abono aplicado correctamente", resp);
    }
}
