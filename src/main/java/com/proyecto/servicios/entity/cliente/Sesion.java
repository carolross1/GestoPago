package com.proyecto.servicios.entity.cliente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Sesion de un login. Guarda el JWT emitido y una bandera "activa" que
 * se apaga cuando pasan mas de N minutos sin actividad (ver
 * SesionActivaFilter). El JWT por si solo no se puede "revocar" una vez
 * emitido, por eso se combina con esta bandera en base de datos: cada
 * peticion protegida valida ambas cosas (firma del JWT + bandera activa).
 */
@Entity
@Table(name = "sesiones")
@Getter
@Setter
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false)
    private Long loginId;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultima_actividad", nullable = false)
    private LocalDateTime ultimaActividad;

    @PrePersist
    void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
