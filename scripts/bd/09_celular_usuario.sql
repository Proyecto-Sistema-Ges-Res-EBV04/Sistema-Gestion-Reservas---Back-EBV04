-- Agranda usuario.celular de 12 a 20 caracteres.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) con usr_agenda.
-- Si dice "sin permisos", repetir conectado con postgres.
-- Se puede ejecutar varias veces.

DO $$
BEGIN
    IF (SELECT character_maximum_length FROM information_schema.columns
        WHERE table_name = 'usuario' AND column_name = 'celular') >= 20 THEN
        RAISE NOTICE 'usuario.celular: ya tenía 20 o más';
    ELSE
        ALTER TABLE public.usuario ALTER COLUMN celular TYPE varchar(20);
        RAISE NOTICE 'usuario.celular: agrandado a 20';
    END IF;
EXCEPTION
    WHEN undefined_table THEN
        RAISE NOTICE 'usuario.celular: NO cambiado, la tabla no existe (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'usuario.celular: NO cambiado, sin permisos (usar postgres)';
END $$;

SELECT table_name AS tabla, column_name AS columna, character_maximum_length AS largo
FROM information_schema.columns
WHERE table_name = 'usuario' AND column_name = 'celular';
