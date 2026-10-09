package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.exception.cliente.CorreoDuplicadoException;
import com.proyecto.servicios.exception.cliente.CurpDuplicadaException;
import com.proyecto.servicios.exception.cliente.RfcDuplicadoException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.cliente.DomicilioRequest;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.DomicilioRepository;
import com.proyecto.servicios.entity.catalogo.Nacionalidad;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import com.proyecto.servicios.service.cliente.Impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DomicilioRepository domicilioRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CuentaService cuentaService;

    @Mock
    private NacionalidadService nacionalidadService;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequest requestValido() {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Juan");
        request.setApellidoPaterno("Perez");
        request.setApellidoMaterno("Lopez");
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setCurp("PELJ990101HDFRPN01");
        request.setRfc("PELJ990101AB1");
        request.setSexo("M");
        request.setNacionalidad("MEX");
        request.setEstadoCivil("SOLTERO");
        request.setCorreo("juan.perez@correo.com");
        request.setTelefonoMovil(5512345678L);
        request.setOcupacion("Ingeniero");
        request.setEmpresa("ACME");
        request.setIngresoMensual(new BigDecimal("15000"));

        DomicilioRequest domicilio = new DomicilioRequest();
        domicilio.setCalle("Reforma");
        domicilio.setNumeroExterior(100);
        domicilio.setColonia("Centro");
        domicilio.setMunicipio("Cuauhtemoc");
        domicilio.setEstado("Ciudad de Mexico");
        domicilio.setCodigoPostal(6000);
        domicilio.setPais("Mexico");
        request.setDomicilio(domicilio);

        return request;
    }

    @Test
    void crear_datosValidos_creaClienteYCuenta() {
        ClienteRequest request = requestValido();

        Nacionalidad mexico = new Nacionalidad();
        mexico.setCodigo("MEX");
        mexico.setNombre("México");
        when(nacionalidadService.buscar("MEX")).thenReturn(Optional.of(mexico));

        when(clienteRepository.existsByCurp(request.getCurp())).thenReturn(false);
        when(clienteRepository.existsByRfc(request.getRfc())).thenReturn(false);
        when(clienteRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("1234567890");
        when(cuentaService.crearCuentaParaCliente(1L)).thenReturn(cuenta);

        ClienteResponse response = clienteService.crear(request);

        assertEquals("1234567890", response.getNumeroCuenta());
        assertEquals("Juan", response.getNombre());
        assertEquals("15000.00", response.getIngresoMensual().toPlainString());
        assertEquals("MEX", response.getNacionalidad());
        assertEquals("México", response.getNacionalidadNombre());
    }

    @Test
    void crear_nacionalidadFueraDeCatalogo_lanzaExcepcion() {
        ClienteRequest request = requestValido();
        request.setNacionalidad("XYZ");
        when(nacionalidadService.validar("XYZ"))
                .thenThrow(new ValidacionNegocioException("La nacionalidad 'XYZ' no existe en el catalogo"));

        assertThrows(ValidacionNegocioException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_menorDeEdad_lanzaValidacionNegocioException() {
        ClienteRequest request = requestValido();
        request.setFechaNacimiento(LocalDate.now().minusYears(17));

        assertThrows(ValidacionNegocioException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_curpDuplicada_lanzaExcepcion() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByCurp(request.getCurp())).thenReturn(true);

        assertThrows(CurpDuplicadaException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_rfcDuplicado_lanzaExcepcion() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByCurp(request.getCurp())).thenReturn(false);
        when(clienteRepository.existsByRfc(request.getRfc())).thenReturn(true);

        assertThrows(RfcDuplicadoException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_correoDuplicado_lanzaExcepcion() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByCurp(request.getCurp())).thenReturn(false);
        when(clienteRepository.existsByRfc(request.getRfc())).thenReturn(false);
        when(clienteRepository.existsByCorreo(request.getCorreo())).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> clienteService.crear(request));
    }

    @Test
    void darDeBaja_marcaInactivoYDesactivaCuenta() {
        Cliente cliente = new Cliente();
        cliente.setId(5L);
        cliente.setActivo(true);
        when(clienteRepository.findById(5L)).thenReturn(Optional.of(cliente));

        clienteService.darDeBaja(5L);

        assertEquals(false, cliente.getActivo());
    }
}
