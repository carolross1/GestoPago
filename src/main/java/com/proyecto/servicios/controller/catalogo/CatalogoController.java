package com.proyecto.servicios.controller.catalogo;

import com.proyecto.servicios.controller.cliente.EjemplosError;
import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.util.Respuestas;
import com.proyecto.servicios.model.catalogo.NacionalidadResponse;
import com.proyecto.servicios.model.catalogo.SincronizacionResponse;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalogos", description = "Catalogo de nacionalidades (origen: restcountries.com)")
@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

    private final NacionalidadService nacionalidadService;

    public CatalogoController(NacionalidadService nacionalidadService) {
        this.nacionalidadService = nacionalidadService;
    }

    @Operation(summary = "Listar nacionalidades (no requiere token)",
            description = "Lee la tabla catalogo_nacionalidades. El 'codigo' es el valor que se envia en "
                    + "el campo nacionalidad al dar de alta un cliente.")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Catalogo ordenado por nombre (vacio si aun no se sincroniza)")
    @GetMapping("/nacionalidades")
    public ResponseEntity<RespuestaApi<List<NacionalidadResponse>>> listarNacionalidades() {
        List<NacionalidadResponse> catalogo = nacionalidadService.listar();
        return Respuestas.ok("Catalogo con " + catalogo.size() + " nacionalidad(es)", catalogo);
    }

    @Operation(summary = "Sincronizar nacionalidades desde restcountries.com",
            description = "Descarga los paises de la API y los guarda/actualiza en la base de datos. "
                    + "Consume ~3 peticiones de la cuota mensual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalogo actualizado"),
            @ApiResponse(responseCode = "502", description = "La API de paises fallo o la API key es invalida",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                            examples = @ExampleObject(value = EjemplosError.EXTERNO)))
    })
    @PostMapping("/nacionalidades/sincronizar")
    public ResponseEntity<RespuestaApi<SincronizacionResponse>> sincronizarNacionalidades() {
        return Respuestas.ok("Catalogo de nacionalidades sincronizado", nacionalidadService.sincronizar());
    }
}
