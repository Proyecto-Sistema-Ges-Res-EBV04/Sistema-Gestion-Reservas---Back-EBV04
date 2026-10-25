-- Table: public.detalleagenda

-- DROP TABLE IF EXISTS public.detalleagenda;

CREATE TABLE IF NOT EXISTS public.detalleagenda
(
    id integer NOT NULL DEFAULT nextval('detalleagenda_id_seq'::regclass),
    fecha date NOT NULL,
    hora time without time zone NOT NULL,
    idiniciosesion integer NOT NULL,
    descripcion character varying COLLATE pg_catalog."default",
    idagenda integer NOT NULL,
    idestadocita integer NOT NULL,
    fechacreacion timestamp without time zone NOT NULL DEFAULT now(),
    fechamodificacion timestamp without time zone,
    iddiaexcepcion bigint,
    CONSTRAINT detalleagenda_pkey PRIMARY KEY (id),
    CONSTRAINT fk_detalleagenda_agenda FOREIGN KEY (idagenda)
        REFERENCES public.agenda (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_detalleagenda_diaexcepcion FOREIGN KEY (iddiaexcepcion)
        REFERENCES public.diaexcepcion (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_detalleagenda_estadocita FOREIGN KEY (idestadocita)
        REFERENCES public.estadocita (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_detalleagenda_iniciosesion FOREIGN KEY (idiniciosesion)
        REFERENCES public.iniciosesion (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.detalleagenda
    OWNER to postgres;

REVOKE ALL ON TABLE public.detalleagenda FROM usr_agenda;

GRANT ALL ON TABLE public.detalleagenda TO postgres;

GRANT INSERT, DELETE, SELECT, UPDATE ON TABLE public.detalleagenda TO usr_agenda;
-- Index: uq_detalleagenda_agenda_fecha_hora

-- DROP INDEX IF EXISTS public.uq_detalleagenda_agenda_fecha_hora;

CREATE UNIQUE INDEX IF NOT EXISTS uq_detalleagenda_agenda_fecha_hora
    ON public.detalleagenda USING btree
    (idagenda ASC NULLS LAST, fecha ASC NULLS LAST, hora ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default
    WHERE idestadocita <> 2 AND idagenda IS NOT NULL;