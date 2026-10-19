package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RecursoRepository recursoRepository;
    private final EspacioEmpleadoRepository espacioEmpleadoRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public ServicioService(ServicioRepository servicioRepository, SedeRepository sedeRepository,
                            ServicioSedeRepository servicioSedeRepository, EmpleadoRepository empleadoRepository,
                            RecursoRepository recursoRepository, EspacioEmpleadoRepository espacioEmpleadoRepository,
                            EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.empleadoRepository = empleadoRepository;
        this.recursoRepository = recursoRepository;
        this.espacioEmpleadoRepository = espacioEmpleadoRepository;
        this.empresaContexto = empresaContexto;
    }

    // RN1 a RN9: registra el servicio completo (datos, sedes, espacios y personal).
    // Primero se valida TODO y solo después se guarda.
    // @Transactional: se hacen muchos INSERT (servicio, serviciosede, recurso, empleado,
    // espacioempleado); si uno falla a mitad de camino, Spring deshace TODOS los cambios
    // de este método (no queda un servicio a medias).
    @Transactional
    public Servicio registrarServicio(RegistrarServicioRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        // 1. Validaciones (no se guarda nada todavía).
        List<Integer> idsSedeVistos = new ArrayList<>();
        List<Sede> sedes = new ArrayList<>();
        for (RegistrarServicioRequest.SedeDelServicio sedeSolicitud : request.getSedes()) {
            // La tabla serviciosede no permite asociar el mismo servicio dos veces a la misma sede.
            if (idsSedeVistos.contains(sedeSolicitud.getIdSede())) {
                throw new ServicioInvalidoException(
                        "La sede " + sedeSolicitud.getIdSede() + " está repetida; selecciónala una sola vez");
            }
            idsSedeVistos.add(sedeSolicitud.getIdSede());

            Sede sede = obtenerSedeActivaDeLaEmpresa(sedeSolicitud.getIdSede(), empresa);
            validarPersonalYEspacios(sede, sedeSolicitud);
            sedes.add(sede);
        }

        // 2. Servicio.
        Servicio servicio = new Servicio();
        servicio.setEmpresa(empresa);
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracion(request.getDuracion());
        servicio.setUnidadDuracion(request.getUnidadDuracion());
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio = servicioRepository.save(servicio);

        // 3. Por cada sede: vínculo, espacios, empleados y los espacios de cada empleado.
        for (int i = 0; i < sedes.size(); i++) {
            Sede sede = sedes.get(i);
            RegistrarServicioRequest.SedeDelServicio sedeSolicitud = request.getSedes().get(i);

            ServicioSede vinculo = new ServicioSede();
            vinculo.setServicio(servicio);
            vinculo.setSede(sede);
            servicioSedeRepository.save(vinculo);

            // Se guarda cada espacio y se recuerda por su identificador para asignarlo luego.
            Map<String, Recurso> espaciosPorClave = new HashMap<>();
            for (String identificador : sedeSolicitud.getEspacios()) {
                Recurso recurso = new Recurso();
                recurso.setServicio(servicio);
                recurso.setSede(sede);
                recurso.setIdentificador(identificador.trim());
                recurso.setExclusivo(true);
                recurso.setFechaCreacion(LocalDateTime.now());
                recurso = recursoRepository.save(recurso);
                espaciosPorClave.put(clave(identificador), recurso);
            }

            for (RegistrarServicioRequest.EmpleadoDelServicio empleadoSolicitud : sedeSolicitud.getEmpleados()) {
                Empleado empleado = new Empleado();
                empleado.setServicio(servicio);
                empleado.setSede(sede);
                empleado.setNombre(empleadoSolicitud.getNombre().trim());
                empleado.setCargo(empleadoSolicitud.getCargo().trim());
                empleado.setFechaCreacion(LocalDateTime.now());
                empleado = empleadoRepository.save(empleado);

                // RN8: un empleado puede tener varios espacios. Si el mismo espacio llega
                // repetido para el mismo empleado se guarda una sola vez (restricción única).
                List<String> clavesAsignadas = new ArrayList<>();
                for (String identificador : empleadoSolicitud.getEspacios()) {
                    String claveEspacio = clave(identificador);
                    if (clavesAsignadas.contains(claveEspacio)) {
                        continue;
                    }
                    clavesAsignadas.add(claveEspacio);
                    espacioEmpleadoRepository.save(
                            new EspacioEmpleado(null, empleado, espaciosPorClave.get(claveEspacio)));
                }
            }
        }

        return servicio;
    }

    // RN5/RN7/RN9: revisa la configuración de una sede antes de guardar nada.
    private void validarPersonalYEspacios(Sede sede, RegistrarServicioRequest.SedeDelServicio sedeSolicitud) {
        // Listas vacías en vez de null, para no tener que preguntar por null más adelante.
        if (sedeSolicitud.getEspacios() == null) {
            sedeSolicitud.setEspacios(new ArrayList<>());
        }
        if (sedeSolicitud.getEmpleados() == null) {
            sedeSolicitud.setEmpleados(new ArrayList<>());
        }

        // RN9: cada espacio con un identificador que lo distinga dentro de la sede.
        List<String> clavesEspacios = new ArrayList<>();
        for (String identificador : sedeSolicitud.getEspacios()) {
            if (identificador == null || identificador.isBlank()) {
                throw new ServicioInvalidoException(
                        "En la sede '" + sede.getNombre() + "' hay un espacio sin nombre o identificador");
            }
            if (clavesEspacios.contains(clave(identificador))) {
                throw new ServicioInvalidoException(
                        "En la sede '" + sede.getNombre() + "' el espacio '" + identificador.trim()
                                + "' está repetido; cada espacio debe tener un identificador distinto");
            }
            clavesEspacios.add(clave(identificador));
        }

        // RN5: al menos un empleado por sede.
        if (sedeSolicitud.getEmpleados().isEmpty()) {
            throw new ServicioInvalidoException(
                    "La sede '" + sede.getNombre() + "' debe tener al menos un empleado asociado al servicio");
        }

        for (RegistrarServicioRequest.EmpleadoDelServicio empleadoSolicitud : sedeSolicitud.getEmpleados()) {
            if (empleadoSolicitud.getEspacios() == null) {
                empleadoSolicitud.setEspacios(new ArrayList<>());
            }

            // RN7: cada empleado con al menos un espacio.
            if (empleadoSolicitud.getEspacios().isEmpty()) {
                throw new ServicioInvalidoException(
                        "En la sede '" + sede.getNombre() + "' el empleado '" + empleadoSolicitud.getNombre()
                                + "' debe tener al menos un espacio asignado");
            }

            // Los espacios del empleado deben ser espacios registrados en esa misma sede.
            for (String identificador : empleadoSolicitud.getEspacios()) {
                if (identificador == null || !clavesEspacios.contains(clave(identificador))) {
                    throw new ServicioInvalidoException(
                            "En la sede '" + sede.getNombre() + "' el empleado '" + empleadoSolicitud.getNombre()
                                    + "' tiene asignado el espacio '" + identificador
                                    + "', que no está registrado en esa sede");
                }
            }
        }
    }

    // "Cabina 1", " cabina 1 " y "CABINA 1" se consideran el mismo espacio.
    private String clave(String identificador) {
        return identificador.trim().toLowerCase();
    }

    // Identificadores de los espacios asignados a un empleado (para la respuesta).
    public List<String> obtenerEspaciosDeEmpleado(Empleado empleado) {
        List<String> identificadores = new ArrayList<>();
        for (EspacioEmpleado espacioEmpleado : espacioEmpleadoRepository.findByEmpleado_Id(empleado.getId())) {
            identificadores.add(espacioEmpleado.getRecurso().getIdentificador());
        }
        return identificadores;
    }

    // RN7/RN8/RN9/RN10
    public Empleado registrarEmpleado(Integer idServicio, Integer idSede, RegistrarEmpleadoRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = validarAsociacion(servicio, idSede);

        Empleado empleado = new Empleado();
        empleado.setServicio(servicio);
        empleado.setSede(sede);
        empleado.setNombre(request.getNombre());
        empleado.setCargo(request.getCargo());
        empleado.setFechaCreacion(LocalDateTime.now());

        return empleadoRepository.save(empleado);
    }

    // RN7/RN8/RN9/RN10
    public Recurso registrarRecurso(Integer idServicio, Integer idSede, RegistrarRecursoRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = validarAsociacion(servicio, idSede);

        Recurso recurso = new Recurso();
        recurso.setServicio(servicio);
        recurso.setSede(sede);
        recurso.setIdentificador(request.getIdentificador());
        recurso.setExclusivo(!Boolean.FALSE.equals(request.getExclusivo()));
        recurso.setFechaCreacion(LocalDateTime.now());

        return recursoRepository.save(recurso);
    }

    public List<Servicio> listarServicios() {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        return servicioRepository.findByEmpresa_Id(empresa.getId());
    }

    public Servicio consultarServicio(Integer idServicio) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        return obtenerServicioDeLaEmpresa(idServicio, empresa);
    }

    public List<Sede> obtenerSedesAsociadas(Servicio servicio) {
        List<ServicioSede> vinculos = servicioSedeRepository.findByServicio_Id(servicio.getId());
        List<Sede> sedes = new ArrayList<>();
        for (ServicioSede vinculo : vinculos) {
            sedes.add(vinculo.getSede());
        }
        return sedes;
    }

    public List<Empleado> obtenerEmpleados(Servicio servicio) {
        return empleadoRepository.findByServicio_Id(servicio.getId());
    }

    public List<Recurso> obtenerRecursos(Servicio servicio) {
        return recursoRepository.findByServicio_Id(servicio.getId());
    }

    private Sede obtenerSedeActivaDeLaEmpresa(Integer idSede, Empresa empresa) {
        Sede sede = sedeRepository.findById(idSede)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new SedeNoEncontradaException(
                        "La sede " + idSede + " no existe o no pertenece a tu Empresa"));

        if (!Boolean.TRUE.equals(sede.getActiva())) {
            throw new SedeInactivaException(
                    "La sede '" + sede.getNombre() + "' no está activa y no puede asociarse a un servicio");
        }
        return sede;
    }

    private Servicio obtenerServicioDeLaEmpresa(Integer idServicio, Empresa empresa) {
        return servicioRepository.findById(idServicio)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new ServicioNoEncontradoException(
                        "El servicio " + idServicio + " no existe o no pertenece a tu Empresa"));
    }

    private Sede validarAsociacion(Servicio servicio, Integer idSede) {
        boolean asociado = servicioSedeRepository.existsByServicio_IdAndSede_Id(servicio.getId(), idSede);
        if (!asociado) {
            throw new AsociacionServicioSedeNoEncontradaException(
                    "La sede " + idSede + " no está asociada al servicio " + servicio.getId());
        }
        return sedeRepository.findById(idSede)
                .orElseThrow(() -> new SedeNoEncontradaException("La sede " + idSede + " no existe"));
    }
}
