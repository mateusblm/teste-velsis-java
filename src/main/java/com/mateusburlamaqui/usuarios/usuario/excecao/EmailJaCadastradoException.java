package com.mateusburlamaqui.usuarios.usuario.excecao;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
