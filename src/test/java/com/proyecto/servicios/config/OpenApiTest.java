package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiTest {

    private final OpenApi openApiConfig = new OpenApi();

    @Test
    @DisplayName("openAPI configura esquema bearerAuth e info")
    void openAPI() {
        OpenAPI api = openApiConfig.openAPI();
        assertNotNull(api);
        assertNotNull(api.getInfo());
        assertEquals("API Integracion de Clientes", api.getInfo().getTitle());
        assertTrue(api.getComponents().getSecuritySchemes().containsKey("bearerAuth"));
    }

    @Test
    @DisplayName("respuesta401EnEndpointsProtegidos agrega 401 a rutas privadas del portal")
    void respuesta401Customizer() {
        OpenApiCustomizer customizer = openApiConfig.respuesta401EnEndpointsProtegidos();
        OpenAPI api = openApiConfig.openAPI();

        Paths paths = new Paths();
        PathItem item = new PathItem();
        Operation op = new Operation();
        item.setPost(op);
        paths.addPathItem("/clientes/buscar", item);
        api.setPaths(paths);

        customizer.customise(api);

        assertTrue(op.getResponses().containsKey("401"));
    }
}
