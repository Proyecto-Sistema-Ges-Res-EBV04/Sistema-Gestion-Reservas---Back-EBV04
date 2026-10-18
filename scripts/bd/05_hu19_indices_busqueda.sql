-- HU-19: indices para las consultas de busqueda de servicios (GET /api/servicios/buscar).
-- Postgres no indexa automaticamente las columnas de llaves foraneas; estos cubren
-- los joins y los filtros por empresa y sede.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) conectado como postgres.
-- No requiere levantar el backend. Se puede ejecutar varias veces.

DO $$
BEGIN
    CREATE INDEX IF NOT EXISTS ix_serviciosede_idsede ON public.serviciosede (idsede);
    CREATE INDEX IF NOT EXISTS ix_sede_idempresa ON public.sede (idempresa);
    CREATE INDEX IF NOT EXISTS ix_servicio_idempresa ON public.servicio (idempresa);
    RAISE NOTICE 'indices de busqueda: aplicados (o ya existian)';
EXCEPTION
    WHEN undefined_table OR undefined_column THEN
        RAISE NOTICE 'indices de busqueda: NO aplicados, faltan tablas o columnas (arrancar el backend)';
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'indices de busqueda: NO aplicados, sin permisos (usar postgres)';
END $$;

SELECT tablename AS tabla, indexname AS indice
FROM pg_indexes
WHERE indexname IN ('ix_serviciosede_idsede', 'ix_sede_idempresa', 'ix_servicio_idempresa')
ORDER BY indexname;
