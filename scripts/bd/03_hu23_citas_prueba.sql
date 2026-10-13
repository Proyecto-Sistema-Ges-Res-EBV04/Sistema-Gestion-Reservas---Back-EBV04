-- HU-23: datos de prueba para "Consultar mis citas" mientras HU-22 (agendar cita) no exista.
-- Crea 3 citas (una próxima, una realizada y una cancelada) para el Usuario del correo indicado,
-- usando el primer servicio asociado a una sede.
-- Requisitos: haber arrancado el backend (crea la tabla cita), un Usuario registrado y un
-- servicio asociado a una sede.
-- Ejecutar en pgAdmin (Query Tool sobre reservas_db) con usr_agenda.
-- CAMBIAR el correo de abajo por el del Usuario con el que se va a probar.

DO $$
DECLARE
    v_correo     TEXT := 'usuario@correo.com';
    v_idusuario  INTEGER;
    v_idservicio INTEGER;
    v_idsede     INTEGER;
BEGIN
    SELECT i.idusuario INTO v_idusuario
      FROM public.iniciosesion i
      JOIN public.tipousuario t ON t.id = i.idtipousuario
     WHERE i.correoelectronico = v_correo AND upper(t.nombre) = 'USUARIO';

    IF v_idusuario IS NULL THEN
        RAISE NOTICE 'No existe un Usuario con el correo %', v_correo;
        RETURN;
    END IF;

    SELECT ss.idservicio, ss.idsede INTO v_idservicio, v_idsede
      FROM public.serviciosede ss
     ORDER BY ss.id
     LIMIT 1;

    IF v_idservicio IS NULL THEN
        RAISE NOTICE 'No hay ningún servicio asociado a una sede';
        RETURN;
    END IF;

    INSERT INTO public.cita (idusuario, idservicio, idsede, fecha, horainicio, horafin, estado, fechacreacion)
    VALUES (v_idusuario, v_idservicio, v_idsede, CURRENT_DATE + 7, '09:00', '09:30', 'PROGRAMADA', now()),
           (v_idusuario, v_idservicio, v_idsede, CURRENT_DATE - 7, '10:00', '10:30', 'PROGRAMADA', now()),
           (v_idusuario, v_idservicio, v_idsede, CURRENT_DATE + 3, '14:00', '14:30', 'CANCELADA', now());

    RAISE NOTICE '3 citas de prueba creadas para el Usuario %', v_idusuario;
END $$;
