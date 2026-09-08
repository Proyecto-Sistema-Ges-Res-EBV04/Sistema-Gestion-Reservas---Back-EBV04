package com.reservas.reservas_backend.integracion;

public class LoginResponse {

    private Integer id;
    private String correo;
    private String tipoUsuario;
    private String token;

    public LoginResponse(Integer id, String correo, String tipoUsuario, String token) {
        this.id = id;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getToken() {
        return token;
    }
}
