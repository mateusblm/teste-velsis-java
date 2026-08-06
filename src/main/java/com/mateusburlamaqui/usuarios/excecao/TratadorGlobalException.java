package com.mateusburlamaqui.usuarios.excecao;

import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}