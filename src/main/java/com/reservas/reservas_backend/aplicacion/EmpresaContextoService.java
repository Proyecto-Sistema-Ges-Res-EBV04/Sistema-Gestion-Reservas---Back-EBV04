package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.infraestructura.EmpresaRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.integracion.EmpresaNoAutenticadaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resuelve la Empresa autenticada a partir del correo que el JwtAuthFilter deja
 * en el SecurityContext. No se modifica JwtAuthFilter/SecurityConfig (son del
 * flujo de login de HU-03): la validación del rol "EMPRESA" se hace aquí,
 * reutilizando InicioSesionRepository tal como lo hace AutenticacionService.
 *
 * Supuesto: TipoUsuario.nombre = "EMPRESA" (mismo literal que debe usar HU-02
 * al registrar la cuenta de empresa). Confirmar con el equipo.
 */
@Service
public class EmpresaContextoService {

    private static final String TIPO_EMPRESA = "EMPRESA";

    private final InicioSesionRepository inicioSesionRepository;
    private final EmpresaRepository empresaRepository;

    @Autowired
    public EmpresaContextoService(InicioSesionRepository inicioSesionRepository,
                                   EmpresaRepository empresaRepository) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.empresaRepository = empresaRepository;
    }

    public Empresa obtenerEmpresaAutenticada() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (!(principal instanceof String)) {
            throw new EmpresaNoAutenticadaException("No hay una sesión válida");
        }
        String correo = (String) principal;

        InicioSesion inicioSesion = inicioSesionRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new EmpresaNoAutenticadaException("No hay una sesión válida"));

        String tipo = inicioSesion.getTipoUsuario() != null ? inicioSesion.getTipoUsuario().getNombre() : "";
        if (!TIPO_EMPRESA.equalsIgnoreCase(tipo)) {
            throw new EmpresaNoAutenticadaException("Solo una Empresa autenticada puede realizar esta acción");
        }

        return empresaRepository.findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new EmpresaNoAutenticadaException("No se encontró la Empresa asociada a la sesión"));
    }
}
