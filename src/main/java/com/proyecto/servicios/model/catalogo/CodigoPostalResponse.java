package com.proyecto.servicios.model.catalogo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoPostalResponse {
    private String codigoPostal;
    private String pais;
    private String estado;
    private String municipio;
    private List<String> colonias;
}
