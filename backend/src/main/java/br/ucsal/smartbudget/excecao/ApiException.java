package br.ucsal.smartbudget.excecao;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final List<String> mensagens;

    public ApiException(HttpStatus status, String mensagem) {
        this(status, List.of(mensagem));
    }

    public ApiException(HttpStatus status, List<String> mensagens) {
        super(String.join(" ", mensagens));
        this.status = status;
        this.mensagens = mensagens;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getMensagens() {
        return mensagens;
    }
}
