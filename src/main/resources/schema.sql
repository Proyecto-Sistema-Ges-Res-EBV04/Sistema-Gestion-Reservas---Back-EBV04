CREATE UNIQUE INDEX IF NOT EXISTS uk_cita_empleado_recurso_fecha_hora
    ON cita (idempleado, idrecurso, fecha, horainicio)
    WHERE estado = 'PROGRAMADA' AND idempleado IS NOT NULL AND idrecurso IS NOT NULL;
