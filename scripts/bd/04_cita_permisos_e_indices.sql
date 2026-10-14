-- HU-22: permisos, indice y restriccion para la tabla cita.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) conectado como postgres,
-- DESPUES de levantar el backend una vez (el backend crea las columnas nuevas de cita).
-- Se puede ejecutar varias veces.

DO $$
BEGIN
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO usr_agenda;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO usr_agenda;
    RAISE NOTICE 'permisos: aplicados a usr_agenda';
EXCEPTION
    WHEN undefined_object THEN
        RAISE NOTICE 'permisos: el rol usr_agenda no existe, se omite';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'permisos: NO aplicados, sin privilegios (usar postgres)';
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ix_cita_servicio_sede_fecha_hora') THEN
        RAISE NOTICE 'ix_cita_servicio_sede_fecha_hora: ya existía';
    ELSE
        CREATE INDEX ix_cita_servicio_sede_fecha_hora
            ON public.cita (idservicio, idsede, fecha, horainicio, estado);
        RAISE NOTICE 'ix_cita_servicio_sede_fecha_hora: agregado';
    END IF;
EXCEPTION
    WHEN undefined_table OR undefined_column THEN
        RAISE NOTICE 'ix_cita_servicio_sede_fecha_hora: NO agregado, falta la tabla o columnas de cita (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'ix_cita_servicio_sede_fecha_hora: NO agregado, sin permisos (usar postgres)';
END $$;

-- Un par empleado + espacio no puede tener dos citas programadas a la misma fecha y hora
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'uk_cita_empleado_recurso_fecha_hora') THEN
        RAISE NOTICE 'uk_cita_empleado_recurso_fecha_hora: ya existía';
    ELSE
        CREATE UNIQUE INDEX uk_cita_empleado_recurso_fecha_hora
            ON public.cita (idempleado, idrecurso, fecha, horainicio)
            WHERE estado = 'PROGRAMADA' AND idempleado IS NOT NULL AND idrecurso IS NOT NULL;
        RAISE NOTICE 'uk_cita_empleado_recurso_fecha_hora: agregado';
    END IF;
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'uk_cita_empleado_recurso_fecha_hora: NO agregado, hay citas repetidas';
    WHEN undefined_table OR undefined_column THEN
        RAISE NOTICE 'uk_cita_empleado_recurso_fecha_hora: NO agregado, falta la tabla o columnas de cita (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'uk_cita_empleado_recurso_fecha_hora: NO agregado, sin permisos (usar postgres)';
END $$;

SELECT tablename AS tabla, indexname AS indice
FROM pg_indexes
WHERE indexname IN ('ix_cita_servicio_sede_fecha_hora', 'uk_cita_empleado_recurso_fecha_hora')
ORDER BY indexname;