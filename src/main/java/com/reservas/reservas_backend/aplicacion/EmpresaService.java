package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.TipoUsuario;
import com.reservas.reservas_backend.infraestructura.EmpresaRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.infraestructura.ServicioSedeRepository;
import com.reservas.reservas_backend.infraestructura.CapacidadAtencionRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.TipoUsuarioRepository;
import com.reservas.reservas_backend.integracion.CorreoYaRegistradoException;
import com.reservas.reservas_backend.integracion.IdentificadorYaRegistradoException;
import com.reservas.reservas_backend.integracion.LoginResponse;
import com.reservas.reservas_backend.integracion.RegistrarEmpresaRequest;
import com.reservas.reservas_backend.integracion.RegistroIncompletoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// HU-02: Registrar cuenta de Empresa (registro por pasos).
@Service
public class EmpresaService {

    private static final String TIPO_EMPRESA = "EMPRESA";

    private final EmpresaRepository empresaRepository;
    private final InicioSesionRepository inicioSesionRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public EmpresaService(EmpresaRepository empresaRepository,
                           InicioSesionRepository inicioSesionRepository,
                           TipoUsuarioRepository tipoUsuarioRepository,
                           SedeRepository sedeRepository,
                           ServicioSedeRepository servicioSedeRepository,
                           CapacidadAtencionRepository capacidadAtencionRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           EmpresaContextoService empresaContexto) {
        this.empresaRepository = empresaRepository;
        this.inicioSesionRepository = inicioSesionRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.empresaContexto = empresaContexto;
    }

    // RN1/RN2/RN6/RN7
    @Transactional
    public LoginResponse registrarEmpresa(RegistrarEmpresaRequest request) {
        if (inicioSesionRepository.existsByCorreoElectronico(request.getCorreo())) {
            throw new CorreoYaRegistradoException("El correo ya está en uso");
        }
        if (empresaRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new IdentificadorYaRegistradoException("La identificación de la Empresa ya está registrada");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(request.getNombre());
        empresa.setIdentificacion(request.getIdentificacion());
        empresa.setTelefono(request.getTelefono());
        empresa.setRegistroCompleto(false);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa = empresaRepository.save(empresa);

        TipoUsuario tipoEmpresa = obtenerOCrearTipoEmpresa();

        InicioSesion inicioSesion = new InicioSesion();
        inicioSesion.setCorreoElectronico(request.getCorreo());
        inicioSesion.setClave(passwordEncoder.encode(request.getClave()));
        inicioSesion.setIdUsuario(empresa.getId());
        inicioSesion.setTipoUsuario(tipoEmpresa);
        inicioSesion.setFechaCreacion(LocalDateTime.now());
        inicioSesion = inicioSesionRepository.save(inicioSesion);

        String token = jwtService.generarToken(inicioSesion.getCorreoElectronico(), TIPO_EMPRESA);

        return new LoginResponse(inicioSesion.getId(), inicioSesion.getCorreoElectronico(), TIPO_EMPRESA, token);
    }

    // RN3/RN4/RN5: valida que la configuración inicial esté completa antes de finalizar el registro.
    @Transactional
    public Empresa finalizarRegistro() {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        List<String> faltantes = new ArrayList<>();
        if (!sedeRepository.existsByEmpresa_Id(empresa.getId())) {
            faltantes.add("al menos una sede registrada");
        }
        if (!servicioSedeRepository.existsByServicio_Empresa_Id(empresa.getId())) {
            faltantes.add("al menos un servicio asociado a una sede");
        }
        if (!capacidadAtencionRepository.existsByServicio_Empresa_Id(empresa.getId())) {
            faltantes.add("al menos una capacidad de atención configurada (mínimo 1)");
        }

        if (!faltantes.isEmpty()) {
            throw new RegistroIncompletoException(
                    "No puedes finalizar el registro, falta: " + String.join("; ", faltantes));
        }

        empresa.setRegistroCompleto(true);
        return empresaRepository.save(empresa);
    }

    private TipoUsuario obtenerOCrearTipoEmpresa() {
        return tipoUsuarioRepository.findByNombre(TIPO_EMPRESA)
                .orElseGet(() -> {
                    TipoUsuario nuevo = new TipoUsuario();
                    nuevo.setNombre(TIPO_EMPRESA);
                    return tipoUsuarioRepository.save(nuevo);
                });
    }
}
