package com.reservaja.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiError {
    private int status;
    private String error;   // ex: "Bad Request"
    private String message; // mensagem amigável
    private String path;    // caminho da requisição (opcional)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // detalhes (opcional) - por exemplo: lista de mensagens de validação
    private List<String> errors;

    // construtor de convenciência
    public ApiError(HttpStatus status, String message, String path, List<String> errors) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
