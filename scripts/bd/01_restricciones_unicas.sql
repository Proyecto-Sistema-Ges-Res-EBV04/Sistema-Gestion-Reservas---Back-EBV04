-- Restricciones UNIQUE para serviciosede, iniciosesion y tipousuario.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) con usr_agenda.
-- Si dice "sin permisos", repetir conectado con postgres.
-- Se puede ejecutar varias veces.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_serviciosede_servicio_sede') THEN
        RAISE NOTICE 'uk_serviciosede_servicio_sede: ya existía';
    ELSE
        ALTER TABLE public.serviciosede
            ADD CONSTRAINT uk_serviciosede_servicio_sede UNIQUE (idservicio, idsede);
        RAISE NOTICE 'uk_serviciosede_servicio_sede: agregada';
    END IF;
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'uk_serviciosede_servicio_sede: NO agregada, hay registros repetidos';
    WHEN undefined_table THEN
        RAISE NOTICE 'uk_serviciosede_servicio_sede: NO agregada, la tabla no existe (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'uk_serviciosede_servicio_sede: NO agregada, sin permisos (usar postgres)';
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_iniciosesion_correo') THEN
        RAISE NOTICE 'uk_iniciosesion_correo: ya existía';
    ELSE
        ALTER TABLE public.iniciosesion
            ADD CONSTRAINT uk_iniciosesion_correo UNIQUE (correoelectronico);
        RAISE NOTICE 'uk_iniciosesion_correo: agregada';
    END IF;
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'uk_iniciosesion_correo: NO agregada, hay correos repetidos';
    WHEN undefined_table THEN
        RAISE NOTICE 'uk_iniciosesion_correo: NO agregada, la tabla no existe (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'uk_iniciosesion_correo: NO agregada, sin permisos (usar postgres)';
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_tipousuario_nombre') THEN
        RAISE NOTICE 'uk_tipousuario_nombre: ya existía';
    ELSE
        ALTER TABLE public.tipousuario
            ADD CONSTRAINT uk_tipousuario_nombre UNIQUE (nombre);
        RAISE NOTICE 'uk_tipousuario_nombre: agregada';
    END IF;
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'uk_tipousuario_nombre: NO agregada, hay nombres repetidos';
    WHEN undefined_table THEN
        RAISE NOTICE 'uk_tipousuario_nombre: NO agregada, la tabla no existe (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'uk_tipousuario_nombre: NO agregada, sin permisos (usar postgres)';
END $$;

SELECT conrelid::regclass AS tabla, conname AS restriccion
FROM pg_constraint
WHERE conname IN ('uk_serviciosede_servicio_sede',
                  'uk_iniciosesion_correo',
                  'uk_tipousuario_nombre')
ORDER BY tabla;
