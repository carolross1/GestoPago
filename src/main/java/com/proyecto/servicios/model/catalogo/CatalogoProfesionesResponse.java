package com.proyecto.servicios.model.catalogo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoProfesionesResponse {

    private List<TabuladorSueldoDto> tabuladores;
    private List<ProfesionDto> profesiones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabuladorSueldoDto {
        private String id;
        private String etiqueta;
        private BigDecimal minimo;
        private BigDecimal maximo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfesionDto {
        private String codigo;
        private String nombre;
        private String categoria;
        private String tabuladorSugerido;
        private BigDecimal sueldoMinimo;
        private BigDecimal sueldoMaximo;
    }
}
