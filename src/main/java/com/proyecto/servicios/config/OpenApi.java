package com.proyecto.servicios.config;

import com.proyecto.servicios.model.GenericResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Configuracion de Swagger (springdoc).
 *
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - Se declara el esquema "bearerAuth" (JWT) para que aparezca el boton
 *   "Authorize": ahi se pega el token que regresa POST /auth/login y
 *   Swagger lo manda en el header Authorization de cada peticion.
 * - No se fija un servidor: Swagger usa el mismo host/puerto desde el que
 *   se abrio, asi "Try it out" funciona aunque cambie el puerto.
 */
@Configuration
public class OpenApi {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Integracion de Clientes")
                        .version("1.0")
                        .description("Alta y consulta de clientes personas fisicas, cuentas y login del portal. "
                                + "Fechas en formato yyyy/MM/dd, horas en HH:mm:ss y cantidades con 2 decimales."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega aqui SOLO el token de /auth/login (sin la palabra Bearer)")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }

    /**
     * Agrega la respuesta 401 a todos los endpoints que exigen token, para
     * no repetirla en cada metodo. Los endpoints publicos (alta de cliente,
     * login y registro) se marcan con @SecurityRequirements vacio, por eso
     * su lista de seguridad viene vacia y aqui se saltan.
     */
    @Bean
    public OpenApiCustomizer respuesta401EnEndpointsProtegidos() {
        return openApi -> {
            Map<String, Schema> esquemas = ModelConverters.getInstance().read(GenericResponse.class);
            esquemas.forEach(openApi.getComponents()::addSchemas);

            ApiResponse sinToken = new ApiResponse()
                    .description("Falta el token, es invalido o la sesion expiro por inactividad")
                    .content(new Content().addMediaType("application/json", new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/GenericResponse"))
                            .addExamples("Sin token", new Example().value(
                                    Map.of("codigo", 401, "mensaje", "Falta el token de autenticacion")))
                            .addExamples("Sesion expirada", new Example().value(
                                    Map.of("codigo", 401, "mensaje", "Sesion expirada por inactividad, inicia sesion nuevamente")))));

            corregirEjemplosDeDinero(openApi);

            openApi.getPaths().forEach((ruta, item) -> item.readOperations().forEach(operacion -> {
                boolean esPublico = operacion.getSecurity() != null && operacion.getSecurity().isEmpty();
                boolean esPortal = ruta.startsWith("/clientes") || ruta.startsWith("/cuentas")
                        || ruta.equals("/catalogos/nacionalidades/sincronizar");
                if (esPortal && !esPublico && !operacion.getResponses().containsKey("401")) {
                    operacion.getResponses().addApiResponse("401", sinToken);
                }
            }));
        };
    }

    /**
     * swagger-core lee el ejemplo de un BigDecimal como numero y lo muestra
     * como "15000.0". Los campos de dinero se marcan con format = "dinero"
     * y aqui se reescribe su ejemplo como texto con exactamente 2 decimales.
     */
    @SuppressWarnings("rawtypes")
    private void corregirEjemplosDeDinero(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        for (Schema esquema : openApi.getComponents().getSchemas().values()) {
            Map<String, Schema> propiedades = esquema.getProperties();
            if (propiedades == null) {
                continue;
            }
            for (Schema propiedad : propiedades.values()) {
                if ("dinero".equals(propiedad.getFormat()) && propiedad.getExample() != null) {
                    String conDosDecimales = new BigDecimal(propiedad.getExample().toString())
                            .setScale(2, RoundingMode.HALF_UP).toPlainString();
                    propiedad.setExample(conDosDecimales);
                    propiedad.setFormat(null);
                    propiedad.setType("string");
                }
            }
        }
    }
}
