package com.proyecto.servicios.model.comun;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/** Resultado paginado de una busqueda. No expone la clase Page de Spring. */
@Getter
public class PaginaResponse<T> {

    @Schema(description = "Elementos de la pagina actual")
    private final List<T> contenido;

    @Schema(description = "Numero de pagina (inicia en 0)", example = "0")
    private final int pagina;

    @Schema(description = "Elementos por pagina", example = "10")
    private final int tamano;

    @Schema(description = "Total de elementos que cumplen los filtros", example = "1")
    private final long totalElementos;

    @Schema(description = "Total de paginas", example = "1")
    private final int totalPaginas;

    public PaginaResponse(Page<T> page) {
        this.contenido = page.getContent();
        this.pagina = page.getNumber();
        this.tamano = page.getSize();
        this.totalElementos = page.getTotalElements();
        this.totalPaginas = page.getTotalPages();
    }
}
