package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.util.CorreoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registro de credenciales de portal para un cliente que YA existe
 * (dado de alta previamente por un ejecutivo via POST /clientes).
 * El cliente se identifica con el mismo correo con el que fue dado de
 * alta, que despues usara para iniciar sesion.
 */
@Data
public class RegistroLoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 100)
    @Schema(example = "juan.perez@correo.com")
    private String correo;

    /** Capa 1: el correo se convierte a minusculas en cuanto llega en el JSON. */
    public void setCorreo(String correo) {
        this.correo = CorreoUtil.normalizar(correo);
    }

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    @Schema(example = "Secreto123")
    private String password;
}
