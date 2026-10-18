-- HU-22: datos de prueba para agendar cita (solo para desarrollo).
-- Requisitos: script 06 (datos de HU-19) ejecutado y backend levantado una vez.
-- Crea personal, espacios y franjas (todos los dias, para poder probar con cualquier
-- fecha futura) en tres casos:
--   - Consulta médica general / Sede Centro: 2 empleados, 2 espacios exclusivos -> capacidad 2
--   - Consulta médica general / Sede Poblado: 2 empleados, 1 espacio compartido -> capacidad 2 (RN7)
--   - Corte de cabello / Sede Belén: franja sin personal -> capacidad 0
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) conectado como postgres.
-- Se puede ejecutar varias veces.

-- Empleados
INSERT INTO public.empleado (nombre, cargo, idservicio, idsede, fechacreacion)
SELECT v.nombre, v.cargo, sv.id, sd.id, now()
FROM (VALUES
    ('Dra. Ana Torres',  'Médica general', 'Consulta médica general', 'Sede Centro'),
    ('Dr. Luis Pérez',   'Médico general', 'Consulta médica general', 'Sede Centro'),
    ('Dra. Marta Gómez', 'Médica general', 'Consulta médica general', 'Sede Poblado'),
    ('Dr. Carlos Ruiz',  'Médico general', 'Consulta médica general', 'Sede Poblado')
) AS v(nombre, cargo, servicio, sede)
JOIN public.empresa e ON e.identificacion = '900111111'
JOIN public.servicio sv ON sv.nombre = v.servicio AND sv.idempresa = e.id
JOIN public.sede sd ON sd.nombre = v.sede AND sd.idempresa = e.id
WHERE NOT EXISTS (SELECT 1 FROM public.empleado x
                  WHERE x.nombre = v.nombre AND x.idservicio = sv.id AND x.idsede = sd.id);

-- Espacios (recursos)
INSERT INTO public.recurso (identificador, exclusivo, idservicio, idsede, fechacreacion)
SELECT v.identificador, v.exclusivo, sv.id, sd.id, now()
FROM (VALUES
    ('Consultorio 1',   true,  'Consulta médica general', 'Sede Centro'),
    ('Consultorio 2',   true,  'Consulta médica general', 'Sede Centro'),
    ('Sala compartida', false, 'Consulta médica general', 'Sede Poblado')
) AS v(identificador, exclusivo, servicio, sede)
JOIN public.empresa e ON e.identificacion = '900111111'
JOIN public.servicio sv ON sv.nombre = v.servicio AND sv.idempresa = e.id
JOIN public.sede sd ON sd.nombre = v.sede AND sd.idempresa = e.id
WHERE NOT EXISTS (SELECT 1 FROM public.recurso r
                  WHERE r.identificador = v.identificador AND r.idservicio = sv.id AND r.idsede = sd.id);

-- Franjas con capacidad (2 pares empleado+espacio en las dos sedes)
INSERT INTO public.capacidadatencion (diasemana, horainicio, horafin, capacidad, idservicio, idsede, fechacreacion)
SELECT d.dia, '08:00'::time, '12:00'::time, 2, sv.id, sd.id, now()
FROM (VALUES ('LUNES'),('MARTES'),('MIERCOLES'),('JUEVES'),('VIERNES'),('SABADO'),('DOMINGO')) AS d(dia)
CROSS JOIN (VALUES ('Consulta médica general', 'Sede Centro'),
                   ('Consulta médica general', 'Sede Poblado')) AS v(servicio, sede)
JOIN public.empresa e ON e.identificacion = '900111111'
JOIN public.servicio sv ON sv.nombre = v.servicio AND sv.idempresa = e.id
JOIN public.sede sd ON sd.nombre = v.sede AND sd.idempresa = e.id
WHERE NOT EXISTS (SELECT 1 FROM public.capacidadatencion c
                  WHERE c.idservicio = sv.id AND c.idsede = sd.id
                    AND c.diasemana = d.dia AND c.horainicio = '08:00'::time);

-- Franja sin personal (capacidad 0): no debe permitir agendar
INSERT INTO public.capacidadatencion (diasemana, horainicio, horafin, capacidad, idservicio, idsede, fechacreacion)
SELECT d.dia, '09:00'::time, '12:00'::time, 0, sv.id, sd.id, now()
FROM (VALUES ('LUNES'),('MARTES'),('MIERCOLES'),('JUEVES'),('VIERNES'),('SABADO'),('DOMINGO')) AS d(dia)
JOIN public.empresa e ON e.identificacion = '900222222'
JOIN public.servicio sv ON sv.nombre = 'Corte de cabello' AND sv.idempresa = e.id
JOIN public.sede sd ON sd.nombre = 'Sede Belén' AND sd.idempresa = e.id
WHERE NOT EXISTS (SELECT 1 FROM public.capacidadatencion c
                  WHERE c.idservicio = sv.id AND c.idsede = sd.id
                    AND c.diasemana = d.dia AND c.horainicio = '09:00'::time);

-- Horarios: cada empleado atiende todas las franjas de su servicio y sede
INSERT INTO public.horarioempleado (idcapacidadatencion, idempleado)
SELECT c.id, x.id
FROM public.capacidadatencion c
JOIN public.empleado x ON x.idservicio = c.idservicio AND x.idsede = c.idsede
WHERE x.nombre IN ('Dra. Ana Torres', 'Dr. Luis Pérez', 'Dra. Marta Gómez', 'Dr. Carlos Ruiz')
  AND NOT EXISTS (SELECT 1 FROM public.horarioempleado h
                  WHERE h.idcapacidadatencion = c.id AND h.idempleado = x.id);

-- Espacios por empleado
INSERT INTO public.espacioempleado (idempleado, idrecurso)
SELECT x.id, r.id
FROM (VALUES
    ('Dra. Ana Torres',  'Consultorio 1'),
    ('Dr. Luis Pérez',   'Consultorio 2'),
    ('Dra. Marta Gómez', 'Sala compartida'),
    ('Dr. Carlos Ruiz',  'Sala compartida')
) AS v(empleado, recurso)
JOIN public.empleado x ON x.nombre = v.empleado
JOIN public.recurso r ON r.identificador = v.recurso
                     AND r.idsede = x.idsede AND r.idservicio = x.idservicio
WHERE NOT EXISTS (SELECT 1 FROM public.espacioempleado ee
                  WHERE ee.idempleado = x.id AND ee.idrecurso = r.id);

-- Verificacion: capacidad esperada de los lunes
SELECT sv.nombre AS servicio, sd.nombre AS sede, c.horainicio, c.horafin, c.capacidad,
       (SELECT count(*) FROM public.horarioempleado h
         WHERE h.idcapacidadatencion = c.id) AS empleados,
       (SELECT count(*) FROM public.horarioempleado h
          JOIN public.espacioempleado ee ON ee.idempleado = h.idempleado
         WHERE h.idcapacidadatencion = c.id) AS pares
FROM public.capacidadatencion c
JOIN public.servicio sv ON sv.id = c.idservicio
JOIN public.sede sd ON sd.id = c.idsede
WHERE c.diasemana = 'LUNES'
  AND sv.nombre IN ('Consulta médica general', 'Corte de cabello')
ORDER BY sv.nombre, sd.nombre;