package com.reservas.reservas_backend.integracion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaInactiva(CuentaInactivaException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaBloqueada(CuentaBloqueadaException ex) {
        return construirRespuesta(HttpStatus.LOCKED, ex.getMessage());
    }

    // HU-09 / HU-11
    @ExceptionHandler(EmpresaNoAutenticadaException.class)
    public ResponseEntity<Map<String, Object>> manejarEmpresaNoAutenticada(EmpresaNoAutenticadaException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SedeNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarSedeNoEncontrada(SedeNoEncontradaException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SedeInactivaException.class)
    public ResponseEntity<Map<String, Object>> manejarSedeInactiva(SedeInactivaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ServicioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioNoEncontrado(ServicioNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AsociacionServicioSedeNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarAsociacionNoEncontrada(AsociacionServicioSedeNoEncontradaException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // HU-01 / HU-02
    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> manejarCorreoYaRegistrado(CorreoYaRegistradoException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IdentificadorYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> manejarIdentificadorYaRegistrado(IdentificadorYaRegistradoException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // HU-02
    @ExceptionHandler(RegistroIncompletoException.class)
    public ResponseEntity<Map<String, Object>> manejarRegistroIncompleto(RegistroIncompletoException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // HU-16
    @ExceptionHandler(CapacidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarCapacidadInvalida(CapacidadInvalidaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Errores de validación de @Valid en el LoginRequest (ej. @Email, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresDeValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensaje);
        return new ResponseEntity<>(body, status);
    }
}
