package com.proyecto.servicios.service.catalogo;

import com.proyecto.servicios.model.catalogo.CatalogoProfesionesResponse;
import com.proyecto.servicios.model.catalogo.CatalogoProfesionesResponse.ProfesionDto;
import com.proyecto.servicios.model.catalogo.CatalogoProfesionesResponse.TabuladorSueldoDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catálogo oficial de profesiones/ocupaciones basado en la clasificación
 * INEGI SINCO (Sistema Nacional de Clasificación de Ocupaciones) de México,
 * junto con su tabulador de rangos salariales mensuales.
 */
@Service
public class ProfesionService {

    private static final List<TabuladorSueldoDto> TABULADORES = List.of(
            tab("TAB_1", "De $0 a $10,000 MXN", "0.01", "10000.00"),
            tab("TAB_2", "De $10,001 a $25,000 MXN", "10001.00", "25000.00"),
            tab("TAB_3", "De $25,001 a $50,000 MXN", "25001.00", "50000.00"),
            tab("TAB_4", "De $50,001 a $100,000 MXN", "50001.00", "100000.00"),
            tab("TAB_5", "De $100,001 a $250,000 MXN", "100001.00", "250000.00")
    );

    private static final List<ProfesionDto> PROFESIONES = List.of(
            prof("SINCO-001", "Estudiante", "Educacion", "TAB_1", "0.01", "10000.00"),
            prof("SINCO-002", "Trabajo del Hogar", "Servicios Personales", "TAB_1", "0.01", "10000.00"),
            prof("SINCO-003", "Jubilado o Pensionado", "Pensiones", "TAB_1", "0.01", "25000.00"),
            prof("SINCO-004", "Auxiliar Administrativo", "Administracion", "TAB_2", "6000.00", "25000.00"),
            prof("SINCO-005", "Cajero o Atencion a Clientes", "Comercio", "TAB_1", "5000.00", "15000.00"),
            prof("SINCO-006", "Comerciante", "Comercio", "TAB_2", "5000.00", "50000.00"),
            prof("SINCO-007", "Vendedor o Ejecutivo de Ventas", "Comercio", "TAB_2", "6000.00", "50000.00"),
            prof("SINCO-008", "Tecnico en Mantenimiento", "Tecnica e Industria", "TAB_2", "8000.00", "25000.00"),
            prof("SINCO-009", "Tecnico en Informatica", "Tecnologia", "TAB_2", "10000.00", "35000.00"),
            prof("SINCO-010", "Electricista o Plomero", "Oficios", "TAB_2", "7000.00", "30000.00"),
            prof("SINCO-011", "Chofer o Operador de Transporte", "Logistica", "TAB_2", "8000.00", "25000.00"),
            prof("SINCO-012", "Docente o Profesor", "Educacion", "TAB_2", "10000.00", "35000.00"),
            prof("SINCO-013", "Enfermero", "Salud", "TAB_2", "10000.00", "35000.00"),
            prof("SINCO-014", "Psicologo", "Salud", "TAB_2", "10000.00", "45000.00"),
            prof("SINCO-015", "Nutriologo", "Salud", "TAB_2", "10000.00", "40000.00"),
            prof("SINCO-016", "Disenador Grafico o Digital", "Creatividad", "TAB_2", "10000.00", "45000.00"),
            prof("SINCO-017", "Contador Publico", "Finanzas", "TAB_3", "12000.00", "50000.00"),
            prof("SINCO-018", "Administrador de Empresas", "Administracion", "TAB_3", "12000.00", "60000.00"),
            prof("SINCO-019", "Analista Financiero", "Finanzas", "TAB_3", "15000.00", "75000.00"),
            prof("SINCO-020", "Abogado", "Derecho", "TAB_3", "14000.00", "80000.00"),
            prof("SINCO-021", "Arquitecto", "Construccion", "TAB_3", "14000.00", "80000.00"),
            prof("SINCO-022", "Ingeniero Civil o Industrial", "Ingenieria", "TAB_3", "15000.00", "85000.00"),
            prof("SINCO-023", "Ingeniero en Sistemas", "Tecnologia", "TAB_3", "18000.00", "100000.00"),
            prof("SINCO-024", "Desarrollador de Software", "Tecnologia", "TAB_3", "18000.00", "100000.00"),
            prof("SINCO-025", "Medico General o Especialista", "Salud", "TAB_4", "20000.00", "150000.00"),
            prof("SINCO-026", "Odontologo", "Salud", "TAB_3", "15000.00", "90000.00"),
            prof("SINCO-027", "Emprendedor o Independiente", "Negocios", "TAB_2", "5000.00", "100000.00"),
            prof("SINCO-028", "Empresario", "Direccion", "TAB_4", "25000.00", "250000.00"),
            prof("SINCO-029", "Gerente de Area", "Direccion", "TAB_4", "30000.00", "150000.00"),
            prof("SINCO-030", "Director Ejecutivo", "Direccion", "TAB_5", "50000.00", "250000.00")
    );

    public CatalogoProfesionesResponse obtenerCatalogo() {
        return CatalogoProfesionesResponse.builder()
                .tabuladores(TABULADORES)
                .profesiones(PROFESIONES)
                .build();
    }

    private static TabuladorSueldoDto tab(String id, String etiqueta, String min, String max) {
        return TabuladorSueldoDto.builder()
                .id(id)
                .etiqueta(etiqueta)
                .minimo(new BigDecimal(min))
                .maximo(new BigDecimal(max))
                .build();
    }

    private static ProfesionDto prof(String codigo, String nombre, String categoria,
                                     String tabuladorSugerido, String min, String max) {
        return ProfesionDto.builder()
                .codigo(codigo)
                .nombre(nombre)
                .categoria(categoria)
                .tabuladorSugerido(tabuladorSugerido)
                .sueldoMinimo(new BigDecimal(min))
                .sueldoMaximo(new BigDecimal(max))
                .build();
    }
}
