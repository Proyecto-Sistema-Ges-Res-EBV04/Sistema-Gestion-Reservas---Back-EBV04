package com.reservas.reservas_backend.config;

import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.TipoUsuario;
import com.reservas.reservas_backend.infraestructura.EmpresaRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.TipoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * TEMPORAL: HU-02 (Registrar cuenta de empresa) todavía no está implementada por el
 * compañero encargado. Mientras tanto, esto siembra UNA Empresa + InicioSesion de
 * prueba para poder desarrollar y probar HU-09 (Registrar sede) y HU-11 (Registrar
 * servicio) sin bloquearse esperando el endpoint real de registro.
 *
 * Es idempotente (no duplica si ya corrió). Bórrese este archivo en cuanto
 * HU-02 quede lista y el equipo tenga un flujo real de registro de Empresa.
 *
 * @Profile("h2"): SOLO se ejecuta cuando la app arranca con
 * -Dspring-boot.run.profiles=h2. Si el equipo corre la app normal (contra el
 * Postgres real de application.properties), este seed NO se activa y la base
 * de datos compartida del equipo queda intacta.
 *
 * Credenciales de prueba (solo válidas corriendo con el perfil h2):
 * empresa.prueba@reservas.test / Empresa123*
 */
@Component
@Profile("h2")
public class SeedEmpresaPruebaRunner implements CommandLineRunner {

    private static final String CORREO_PRUEBA = "empresa.prueba@reservas.test";
    private static final String CLAVE_PRUEBA = "Empresa123*";

    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final InicioSesionRepository inicioSesionRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SeedEmpresaPruebaRunner(TipoUsuarioRepository tipoUsuarioRepository,
                                    EmpresaRepository empresaRepository,
                                    InicioSesionRepository inicioSesionRepository,
                                    SedeRepository sedeRepository,
                                    PasswordEncoder passwordEncoder) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.empresaRepository = empresaRepository;
        this.inicioSesionRepository = inicioSesionRepository;
        this.sedeRepository = sedeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (inicioSesionRepository.existsByCorreoElectronico(CORREO_PRUEBA)) {
            return;
        }

        TipoUsuario tipoEmpresa = tipoUsuarioRepository.findByNombre("EMPRESA")
                .orElseGet(() -> {
                    TipoUsuario nuevo = new TipoUsuario();
                    nuevo.setNombre("EMPRESA");
                    return tipoUsuarioRepository.save(nuevo);
                });

        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa de Prueba HU09-HU11");
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa = empresaRepository.save(empresa);

        InicioSesion inicioSesion = new InicioSesion();
        inicioSesion.setCorreoElectronico(CORREO_PRUEBA);
        inicioSesion.setClave(passwordEncoder.encode(CLAVE_PRUEBA));
        inicioSesion.setIdUsuario(empresa.getId());
        inicioSesion.setTipoUsuario(tipoEmpresa);
        inicioSesion.setFechaCreacion(LocalDateTime.now());
        inicioSesionRepository.save(inicioSesion);

        // Sede inactiva SOLO para verificar manualmente el escenario "Sede no disponible" (RN4 HU-11).
        // Quitar junto con el resto de este seed cuando HU-02 esté lista.
        Sede sedeInactiva = new Sede();
        sedeInactiva.setEmpresa(empresa);
        sedeInactiva.setNombre("Sede Inactiva (prueba)");
        sedeInactiva.setDireccion("Calle Falsa 123");
        sedeInactiva.setMunicipio("Medellin");
        sedeInactiva.setTelefono("3000000000");
        sedeInactiva.setActiva(false);
        sedeInactiva.setFechaCreacion(LocalDateTime.now());
        sedeRepository.save(sedeInactiva);

        System.out.println("[SEED] Empresa de prueba creada -> correo: " + CORREO_PRUEBA + " / clave: " + CLAVE_PRUEBA);
    }
}
