package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.excecao.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadorDeErros {
    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> erroDaApi(ApiException e) {
        return resposta(e.getStatus(), e.getMensagens());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        List<String> mensagens = e.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getDefaultMessage())
                .distinct()
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, mensagens);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> formatoInvalido(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, List.of("Dados em formato inválido. Confira o JSON, "
                + "as datas (AAAA-MM-DD) e os valores permitidos."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> enderecoInexistente(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, List.of("Endereço não encontrado na API."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, List.of("Método " + e.getMethod() + " não aceito neste endereço."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Ocorreu um erro inesperado. Tente novamente."));
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, List<String> mensagens) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), titulo(status), mensagens, LocalDateTime.now()));
    }

    private String titulo(HttpStatus status) {
        return switch (status.value()) {
            case 400 -> "Dados inválidos";
            case 401 -> "Não autenticado";
            case 403 -> "Acesso negado";
            case 404 -> "Não encontrado";
            case 405 -> "Método não permitido";
            case 409 -> "Conflito";
            case 423 -> "Viagem bloqueada";
            case 503 -> "IA indisponível";
            default -> "Erro interno";
        };
    }

    public record ErroResponse(int status, String erro, List<String> mensagens, LocalDateTime horario) {
    }
}
