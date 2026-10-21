package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.util.CorreoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.proyecto.servicios.model.comun.SolicitudEstricta;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inicio de sesion del portal con correo y contrasena.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginRequest extends SolicitudEstricta {

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
    @Schema(example = "Secreto123")
    private String password;
}
