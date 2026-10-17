-- HU-09: el teléfono de la sede pasa a ser opcional.
-- Hibernate (ddl-auto=update) agrega columnas nuevas, pero NO le quita el NOT NULL
-- a una columna que ya existe; por eso este cambio va en un script aparte.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) con usr_agenda.
-- Si dice "sin permisos", repetir conectado con postgres.
-- Se puede ejecutar varias veces.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'sede'
                 AND column_name = 'telefono' AND is_nullable = 'NO') THEN
        ALTER TABLE public.sede ALTER COLUMN telefono DROP NOT NULL;
        RAISE NOTICE 'sede.telefono: ahora es opcional';
    ELSE
        RAISE NOTICE 'sede.telefono: ya era opcional (o la tabla no existe, arrancar el backend)';
    END IF;
EXCEPTION
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'sede.telefono: NO modificada, sin permisos (usar postgres)';
END $$;

SELECT column_name, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'sede'
  AND column_name IN ('telefono', 'pais', 'descripcion', 'referencia')
ORDER BY column_name;
