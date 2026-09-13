package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.UsuarioRepository;
import com.reservas.reservas_backend.integracion.PerfilResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilService {

    private final InicioSesionRepository inicioSesionRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(InicioSesionRepository inicioSesionRepository,
                         UsuarioRepository usuarioRepository) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public PerfilResponse consultarPerfil(String correo) {
        InicioSesion inicioSesion = inicioSesionRepository
                .findByCorreoElectronico(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró la sesión del usuario"));

        Usuario usuario = usuarioRepository
                .findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el perfil del usuario"));

        return new PerfilResponse(
                usuario.getId(),
                usuario.getPrimerNombre(),
                usuario.getSegundoNombre(),
                usuario.getPrimerApellido(),
                usuario.getSegundoApellido(),
                usuario.getDireccion(),
                usuario.getIdMunicipio(),
                usuario.getCelular(),
                usuario.getFechaNacimiento()
        );
    }
}
