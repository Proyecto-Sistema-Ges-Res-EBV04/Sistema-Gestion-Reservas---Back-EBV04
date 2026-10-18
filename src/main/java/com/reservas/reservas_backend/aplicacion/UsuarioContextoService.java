package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.UsuarioRepository;
import com.reservas.reservas_backend.integracion.UsuarioNoAutenticadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Resuelve el Usuario autenticado a partir del correo que el JwtAuthFilter deja en el
// SecurityContext (mismo enfoque que EmpresaContextoService, pero para el rol "USUARIO").
@Service
public class UsuarioContextoService {

    private static final String TIPO_USUARIO = "USUARIO";

    private final InicioSesionRepository inicioSesionRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioContextoService(InicioSesionRepository inicioSesionRepository,
                                  UsuarioRepository usuarioRepository) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (!(principal instanceof String)) {
            throw new UsuarioNoAutenticadoException("No hay una sesión válida");
        }
        String correo = (String) principal;

        InicioSesion inicioSesion = inicioSesionRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new UsuarioNoAutenticadoException("No hay una sesión válida"));

        String tipo = inicioSesion.getTipoUsuario() != null ? inicioSesion.getTipoUsuario().getNombre() : "";
        if (!TIPO_USUARIO.equalsIgnoreCase(tipo)) {
            throw new UsuarioNoAutenticadoException("Solo un Usuario autenticado puede realizar esta acción");
        }

        return usuarioRepository.findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new UsuarioNoAutenticadoException("No se encontró el Usuario asociado a la sesión"));
    }
}
