package com.mateusburlamaqui.usuarios.excecao;

import com.mateusburlamaqui.usuarios.usuario.excecao.EmailJaCadastradoException;
import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Centraliza a conversão das exceções da aplicação
@RestControllerAdvice
public class TratadorGlobalException {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarUsuarioNaoEncontrado(UsuarioNaoEncontradoException excecao) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErroResponse erro = new ErroResponse(LocalDateTime.now(),status.value(),excecao.getMessage(),Map.of());

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarDadosInvalidos(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();

        excecao.getBindingResult().getFieldErrors().forEach(erro ->
            campos.put(
                    erro.getField(),
                    erro.getDefaultMessage()
                    )
        );

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErroResponse resposta = new ErroResponse(LocalDateTime.now(),status.value(),"Dados inválidos.",campos);

        return ResponseEntity.status(status).body(resposta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoDaRequisicaoInvalido(HttpMessageNotReadableException excecao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), "Corpo da requisição inválido.", Map.of());

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException excecao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> campos = Map.of(excecao.getName(), "O parâmetro informado é inválido.");

        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), "Parâmetro inválido.", campos);

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> tratarEmailJaCadastrado(EmailJaCadastradoException excecao) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), excecao.getMessage(), Map.of());

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarViolacaoDeIntegridade(DataIntegrityViolationException excecao) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), "Já existe um usuário cadastrado com o e-mail informado.", Map.of());

        return ResponseEntity.status(status).body(erro);
    }
}
