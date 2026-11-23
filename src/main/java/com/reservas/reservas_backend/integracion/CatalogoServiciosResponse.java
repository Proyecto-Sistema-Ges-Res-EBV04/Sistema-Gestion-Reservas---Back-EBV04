package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CatalogoServiciosResponse {
    private String mensaje;   // "No existen servicios registrados" si la lista está vacía; si no, null
    private List<ServicioCatalogoResponse> servicios;
}