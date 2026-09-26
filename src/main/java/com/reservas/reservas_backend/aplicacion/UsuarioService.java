package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.TipoUsuario;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.TipoUsuarioRepository;
import com.reservas.reservas_backend.infraestructura.UsuarioRepository;
import com.reservas.reservas_backend.integracion.CorreoYaRegistradoException;
import com.reservas.reservas_backend.integracion.IdentificadorYaRegistradoException;
import com.reservas.reservas_backend.integracion.LoginResponse;
import com.reservas.reservas_backend.integracion.RegistrarUsuarioRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// HU-01: Registrar cuenta de Usuario.
@Service
public class UsuarioService {

    private static final String TIPO_USUARIO = "USUARIO";

    private final UsuarioRepository usuarioRepository;
    private final InicioSesionRepository inicioSesionRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                           InicioSesionRepository inicioSesionRepository,
                           TipoUsuarioRepository tipoUsuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.inicioSesionRepository = inicioSesionRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // RN1/RN2/RN3/RN4/RN5
    @Transactional
    public LoginResponse registrarUsuario(RegistrarUsuarioRequest request) {
        if (inicioSesionRepository.existsByCorreoElectronico(request.getCorreo())) {
            throw new CorreoYaRegistradoException("El correo ya está en uso");
        }
        if (usuarioRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new IdentificadorYaRegistradoException("El documento de identidad ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNumeroDocumento(request.getNumeroDocumento());
        usuario.setPrimerNombre(request.getPrimerNombre());
        usuario.setSegundoNombre(request.getSegundoNombre());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setDireccion(request.getDireccion());
        usuario.setIdMunicipio(request.getIdMunicipio());
        usuario.setCelular(request.getCelular());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        TipoUsuario tipoUsuario = obtenerOCrearTipoUsuario();

        InicioSesion inicioSesion = new InicioSesion();
        inicioSesion.setCorreoElectronico(request.getCorreo());
        inicioSesion.setClave(passwordEncoder.encode(request.getClave()));
        inicioSesion.setIdUsuario(usuario.getId());
        inicioSesion.setTipoUsuario(tipoUsuario);
        inicioSesion.setFechaCreacion(LocalDateTime.now());
        inicioSesion = inicioSesionRepository.save(inicioSesion);

        String token = jwtService.generarToken(inicioSesion.getCorreoElectronico(), TIPO_USUARIO);

        return new LoginResponse(inicioSesion.getId(), inicioSesion.getCorreoElectronico(), TIPO_USUARIO, token);
    }

    private TipoUsuario obtenerOCrearTipoUsuario() {
        return tipoUsuarioRepository.findByNombre(TIPO_USUARIO)
                .orElseGet(() -> {
                    TipoUsuario nuevo = new TipoUsuario();
                    nuevo.setNombre(TIPO_USUARIO);
                    return tipoUsuarioRepository.save(nuevo);
                });
    }
}
