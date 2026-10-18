package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.EmpresaRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.ServicioRepository;
import com.reservas.reservas_backend.infraestructura.UsuarioRepository;
import com.reservas.reservas_backend.integracion.PerfilNegocioResponse;
import com.reservas.reservas_backend.integracion.PerfilUsuarioResponse;
import com.reservas.reservas_backend.integracion.SedeResponse;
import com.reservas.reservas_backend.integracion.ServicioResumenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * HU-06: Consultar perfil.
 *
 *   - Cuenta de tipo "EMPRESA": inicioSesion.idUsuario apunta al id de la
 *     Empresa (igual que en EmpresaContextoService). Se muestra la info de
 *     la Empresa, sus sedes y sus servicios (RN3).
 *   - Cualquier otro tipo (Usuario, Administrador): inicioSesion.idUsuario
 *     apunta a la tabla "usuario" y se muestra la info básica (RN2).
 */
@Service
public class PerfilService {

    private static final String TIPO_EMPRESA = "EMPRESA";

    private final InicioSesionRepository inicioSesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SedeRepository sedeRepository;
    private final ServicioRepository servicioRepository;

    public PerfilService(InicioSesionRepository inicioSesionRepository,
                          UsuarioRepository usuarioRepository,
                          EmpresaRepository empresaRepository,
                          SedeRepository sedeRepository,
                          ServicioRepository servicioRepository) {
        this.inicioSesionRepository = inicioSesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.sedeRepository = sedeRepository;
        this.servicioRepository = servicioRepository;
    }

    public Object consultarPerfil(String correo) {
        InicioSesion inicioSesion = inicioSesionRepository
                .findByCorreoElectronico(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró la sesión del usuario"));

        String tipo = inicioSesion.getTipoUsuario() != null
                ? inicioSesion.getTipoUsuario().getNombre()
                : "";

        if (TIPO_EMPRESA.equalsIgnoreCase(tipo)) {
            return consultarPerfilEmpresa(inicioSesion);
        }

        // Usuario y Administrador comparten la misma tabla "usuario" (RN4:
        // el admin solo ve información básica de cuenta, igual que un Usuario).
        return consultarPerfilUsuario(inicioSesion);
    }

    private PerfilUsuarioResponse consultarPerfilUsuario(InicioSesion inicioSesion) {
        Usuario usuario = usuarioRepository
                .findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el perfil del usuario"));

        return new PerfilUsuarioResponse(
                usuario.getId(),
                usuario.getNumeroDocumento(),
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

    private PerfilNegocioResponse consultarPerfilEmpresa(InicioSesion inicioSesion) {
        Empresa empresa = empresaRepository
                .findById(inicioSesion.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el perfil de la empresa"));

        List<Sede> sedes = sedeRepository.findByEmpresa_Id(empresa.getId());

        List<SedeResponse> sedesResponse = new ArrayList<>();
        for (Sede sede : sedes) {
            sedesResponse.add(SedeResponse.desde(sede));
        }

        List<Servicio> servicios = servicioRepository.findByEmpresa_Id(empresa.getId());

        List<ServicioResumenResponse> serviciosResponse = new ArrayList<>();
        for (Servicio servicio : servicios) {
            serviciosResponse.add(ServicioResumenResponse.desde(servicio));
        }

        return new PerfilNegocioResponse(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getIdentificacion(),
                inicioSesion.getCorreoElectronico(),
                empresa.getTelefono(),
                sedesResponse,
                serviciosResponse
        );
    }
}
