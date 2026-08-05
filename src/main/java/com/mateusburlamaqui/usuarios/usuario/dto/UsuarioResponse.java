package com.mateusburlamaqui.usuarios.usuario.dto;

import com.mateusburlamaqui.usuarios.usuario.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    } 
}