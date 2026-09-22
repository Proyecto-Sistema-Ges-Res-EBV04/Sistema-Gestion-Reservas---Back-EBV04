package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Negocio;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.NegocioRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.UsuarioRepository;
import com.reservas.reservas_backend.integracion.PerfilNegocioResponse;
import com.reservas.reservas_backend.integracion.PerfilUsuarioResponse;
import com.reservas.reservas_backend.integracion.SedeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * HU-06: Consultar perfil.
 *
 * Según el esquema real de la base de datos:
 *   - Toda cuenta (iniciosesion) siempre tiene un "usuario" asociado
 *     (idusuario), sin importar el tipo de cuenta.
 *   - Si el tipo de cuenta es "Negocio"/"Empresa", ADEMÁS existe un
 *     registro en "negocio" que se relaciona por idiniciosesion (no por
 *     idusuario). Ese negocio tiene sus propias sedes.
 *   - No existe una tabla separada de "Administrador": una cuenta de
 *     administrador usa la misma información básica de "usuario"
 *     (RN4: solo info básica de cuenta, sin funciones adicionales).
 *
 * IMPORTANTE: el valor real de tipousuario.nombre para "negocio" no estaba
 * disponible en el volcado de solo-esquema (no incluye datos). Se compara
 * aceptando "NEGOCIO" o "EMPRESA" (sin distinguir mayúsculas) para cubrir
 * ambos casos comunes. Verifica el valor real en tu tabla tipousuario y
 * ajusta ES_TIPO_NEGOCIO si hace falta.
 */
@Service
public class PerfilService {

    private final InicioSesionRepository inicioSesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final SedeRepository sedeRepository;

    public PerfilService(InicioSesionRepository inicioSesionRepository,
                          UsuarioRepository usuarioRepository,
                          NegocioRepository negocioRepository,
                          SedeRepository sedeRepository) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.sedeRepository = sedeRepository;
    }

    public Object consultarPerfil(String correo) {
        InicioSesion inicioSesion = inicioSesionRepository
                .findByCorreoElectronico(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró la sesión del usuario"));

        String tipo = inicioSesion.getTipoUsuario() != null
                ? inicioSesion.getTipoUsuario().getNombre()
                : "";

        if (esTipoNegocio(tipo)) {
            return consultarPerfilNegocio(inicioSesion);
        }

        // Usuario y Administrador comparten la misma tabla "usuario" (RN4:
        // el admin solo ve información básica de cuenta, igual que un Usuario).
        return consultarPerfilUsuario(inicioSesion);
    }

    private boolean esTipoNegocio(String tipo) {
        return "NEGOCIO".equalsIgnoreCase(tipo) || "EMPRESA".equalsIgnoreCase(tipo);
    }

    private PerfilUsuarioResponse consultarPerfilUsuario(InicioSesion inicioSesion) {
        Usuario usuario = usuarioRepository
                .findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el perfil del usuario"));

        return new PerfilUsuarioResponse(
                usuario.getId(),
                usuario.getPrimerNombre(),
                usuario.getSegundoNombre(),
                usuario.getPrimerApellido(),
                usuario.getSegundoApellido(),
                inicioSesion.getCorreoElectronico(),
                usuario.getDireccion(),
                usuario.getIdMunicipio(),
                usuario.getCelular(),
                usuario.getFechaNacimiento()
        );
    }

    private PerfilNegocioResponse consultarPerfilNegocio(InicioSesion inicioSesion) {
        Negocio negocio = negocioRepository
                .findByIdIniciaSesion(inicioSesion.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el perfil del negocio"));

        List<Sede> sedes = sedeRepository.findByIdNegocio(negocio.getId());

        List<SedeResponse> sedesResponse = sedes.stream()
                .map(sede -> new SedeResponse(
                        sede.getId(),
                        sede.getNombre(),
                        sede.getDireccion(),
                        sede.getIdMunicipio()))
                .toList();

        return new PerfilNegocioResponse(
                negocio.getId(),
                negocio.getNombre(),
                negocio.getRazonSocial(),
                inicioSesion.getCorreoElectronico(),
                negocio.getTelefono(),
                negocio.getCelular(),
                sedesResponse
        );
    }
}
