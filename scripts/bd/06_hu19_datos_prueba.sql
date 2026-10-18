-- HU-19: datos de prueba para la busqueda de servicios (solo para desarrollo).
-- Crea 3 empresas, 5 sedes, 4 servicios y sus asociaciones, incluyendo dos casos
-- que NO deben aparecer en la busqueda:
--   - 'Sede Antigua' esta inactiva
--   - 'Spa Incompleto' tiene registro incompleto
-- Requisito: haber levantado el backend una vez (crea las tablas).
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) conectado como postgres.
-- Guardar en UTF-8 (tildes). Se puede ejecutar varias veces.

-- Ajuste heredado del backup original: sede traia idnegocio e idmunicipio obligatorias,
-- que la entidad actual no llena.
DO $$
BEGIN
    ALTER TABLE public.sede ALTER COLUMN idnegocio DROP NOT NULL;
    ALTER TABLE public.sede ALTER COLUMN idmunicipio DROP NOT NULL;
    RAISE NOTICE 'sede: columnas heredadas del backup ahora opcionales';
EXCEPTION
    WHEN undefined_column THEN
        RAISE NOTICE 'sede: sin columnas heredadas del backup, nada que ajustar';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'sede: NO ajustada, sin permisos (usar postgres)';
END $$;

INSERT INTO public.empresa (nombre, identificacion, telefono, registrocompleto, fechacreacion)
SELECT v.nombre, v.identificacion, v.telefono, v.registrocompleto, now()
FROM (VALUES
    ('Clínica Salud Total', '900111111', '6041111111', true),
    ('Salón Belleza Viva',  '900222222', '6042222222', true),
    ('Spa Incompleto',      '900333333', '6043333333', false)
) AS v(nombre, identificacion, telefono, registrocompleto)
WHERE NOT EXISTS (SELECT 1 FROM public.empresa e WHERE e.identificacion = v.identificacion);

INSERT INTO public.sede (nombre, direccion, municipio, telefono, activa, idempresa, fechacreacion)
SELECT v.nombre, v.direccion, v.municipio, v.telefono, v.activa, e.id, now()
FROM (VALUES
    ('Sede Centro',   'Calle 50 # 45-10',    'Medellín', '6041111111', true,  '900111111'),
    ('Sede Poblado',  'Carrera 43A # 10-20', 'Medellín', '6041111112', true,  '900111111'),
    ('Sede Antigua',  'Calle 10 # 5-5',      'Bello',    '6041111113', false, '900111111'),
    ('Sede Belén',    'Calle 30 # 70-15',    'Medellín', '6042222222', true,  '900222222'),
    ('Sede Sabaneta', 'Carrera 45 # 70-30',  'Sabaneta', '6043333333', true,  '900333333')
) AS v(nombre, direccion, municipio, telefono, activa, identificacion)
JOIN public.empresa e ON e.identificacion = v.identificacion
WHERE NOT EXISTS (SELECT 1 FROM public.sede s WHERE s.nombre = v.nombre AND s.idempresa = e.id);

INSERT INTO public.servicio (nombre, descripcion, duracion, unidadduracion, idempresa, fechacreacion)
SELECT v.nombre, v.descripcion, v.duracion, v.unidadduracion, e.id, now()
FROM (VALUES
    ('Consulta médica general', 'Valoración médica general',          30, 'MINUTOS', '900111111'),
    ('Odontología',             'Limpieza y valoración odontológica', 45, 'MINUTOS', '900111111'),
    ('Corte de cabello',        'Corte y peinado',                     1, 'HORAS',   '900222222'),
    ('Masaje relajante',        'Masaje de cuerpo completo',           1, 'HORAS',   '900333333')
) AS v(nombre, descripcion, duracion, unidadduracion, identificacion)
JOIN public.empresa e ON e.identificacion = v.identificacion
WHERE NOT EXISTS (SELECT 1 FROM public.servicio s WHERE s.nombre = v.nombre AND s.idempresa = e.id);

INSERT INTO public.serviciosede (idservicio, idsede)
SELECT sv.id, sd.id
FROM (VALUES
    ('Consulta médica general', 'Sede Centro',   '900111111'),
    ('Consulta médica general', 'Sede Poblado',  '900111111'),
    ('Odontología',             'Sede Centro',   '900111111'),
    ('Odontología',             'Sede Antigua',  '900111111'),
    ('Corte de cabello',        'Sede Belén',    '900222222'),
    ('Masaje relajante',        'Sede Sabaneta', '900333333')
) AS v(servicio, sede, identificacion)
JOIN public.empresa e ON e.identificacion = v.identificacion
JOIN public.servicio sv ON sv.nombre = v.servicio AND sv.idempresa = e.id
JOIN public.sede sd ON sd.nombre = v.sede AND sd.idempresa = e.id
WHERE NOT EXISTS (SELECT 1 FROM public.serviciosede ss WHERE ss.idservicio = sv.id AND ss.idsede = sd.id);

SELECT s.nombre AS servicio, e.nombre AS empresa, sd.nombre AS sede,
       sd.municipio, sd.activa AS sede_activa, e.registrocompleto
FROM public.serviciosede ss
JOIN public.servicio s ON s.id = ss.idservicio
JOIN public.sede sd ON sd.id = ss.idsede
JOIN public.empresa e ON e.id = s.idempresa
WHERE e.identificacion IN ('900111111', '900222222', '900333333')
ORDER BY e.nombre, s.nombre, sd.nombre;