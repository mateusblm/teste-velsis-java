package com.mateusburlamaqui.usuarios.usuario.excecao;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }
}