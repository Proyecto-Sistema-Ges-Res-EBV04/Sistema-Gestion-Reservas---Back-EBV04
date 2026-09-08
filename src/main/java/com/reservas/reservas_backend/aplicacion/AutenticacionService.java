package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.BloqueoInicioSesion;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.infraestructura.BloqueoInicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.integracion.CredencialesInvalidasException;
import com.reservas.reservas_backend.integracion.CuentaBloqueadaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AutenticacionService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    private final InicioSesionRepository inicioSesionRepository;
    private final BloqueoInicioSesionRepository bloqueoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AutenticacionService(InicioSesionRepository inicioSesionRepository,
                                 BloqueoInicioSesionRepository bloqueoRepository,
                                 PasswordEncoder passwordEncoder) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public InicioSesion iniciarSesion(String correo, String contrasenaIngresada) {

        InicioSesion inicioSesion = inicioSesionRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new CredencialesInvalidasException("Las credenciales no son válidas"));

        BloqueoInicioSesion bloqueo = obtenerOCrearBloqueo(inicioSesion);

        // RN4: verificar si está bloqueada y si el bloqueo ya expiró
        if (Boolean.TRUE.equals(bloqueo.getBloqueado())) {
            if (bloqueo.getFechaDesbloqueo() != null && bloqueo.getFechaDesbloqueo().isBefore(LocalDateTime.now())) {
                // Ya pasaron los 15 minutos: se desbloquea automáticamente
                bloqueo.setBloqueado(false);
                bloqueo.setContador(0);
                bloqueo.setFechaDesbloqueo(null);
                bloqueoRepository.save(bloqueo);
            } else {
                throw new CuentaBloqueadaException("La cuenta está bloqueada temporalmente. Intenta de nuevo más tarde");
            }
        }

        boolean contrasenaValida = passwordEncoder.matches(contrasenaIngresada, inicioSesion.getClave());

        if (!contrasenaValida) {
            registrarIntentoFallido(bloqueo);
            throw new CredencialesInvalidasException("Las credenciales no son válidas");
        }

        // Login exitoso: reiniciar contador
        bloqueo.setContador(0);
        bloqueo.setBloqueado(false);
        bloqueo.setFechaDesbloqueo(null);
        bloqueoRepository.save(bloqueo);

        return inicioSesion;
    }

    private BloqueoInicioSesion obtenerOCrearBloqueo(InicioSesion inicioSesion) {
        return bloqueoRepository.findByInicioSesion_Id(inicioSesion.getId())
                .orElseGet(() -> {
                    BloqueoInicioSesion nuevo = new BloqueoInicioSesion();
                    nuevo.setInicioSesion(inicioSesion);
                    nuevo.setContador(0);
                    nuevo.setBloqueado(false);
                    return bloqueoRepository.save(nuevo);
                });
    }

    private void registrarIntentoFallido(BloqueoInicioSesion bloqueo) {
        int intentos = bloqueo.getContador() + 1;
        bloqueo.setContador(intentos);

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            bloqueo.setBloqueado(true);
            bloqueo.setFechaDesbloqueo(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }

        bloqueoRepository.save(bloqueo);
    }
}
