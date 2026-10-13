-- HU-16: la capacidad pasa a calcularse a partir de horarios y espacios del personal.
-- Elimina las tablas del modelo anterior (capacidad ingresada a mano + límite simultáneo)
-- y deja en 0 las franjas existentes para que se vuelvan a configurar.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) con usr_agenda.
-- Si dice "sin permisos", repetir conectado con postgres.
-- Se puede ejecutar varias veces.

DROP TABLE IF EXISTS public.capacidadrecurso;
DROP TABLE IF EXISTS public.capacidadempleado;

DO $$
BEGIN
    UPDATE public.capacidadatencion c
       SET capacidad = 0
     WHERE NOT EXISTS (SELECT 1 FROM public.horarioempleado h WHERE h.idcapacidadatencion = c.id);
    RAISE NOTICE 'capacidadatencion: franjas sin personal asignado quedaron con capacidad 0';
EXCEPTION
    WHEN undefined_table THEN
        RAISE NOTICE 'capacidadatencion: NO actualizada, falta alguna tabla (arrancar el backend)';
END $$;
