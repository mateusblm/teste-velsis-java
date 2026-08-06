package com.mateusburlamaqui.usuarios.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mateusburlamaqui.usuarios.email.EmailService;
import com.mateusburlamaqui.usuarios.usuario.dto.AtualizarUsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioService(UsuarioRepository usuarioRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest usuarioRequest) {
        Usuario usuario = new Usuario(
            usuarioRequest.nome().trim(),
            usuarioRequest.email().trim().toLowerCase(),
            passwordEncoder.encode(usuarioRequest.senha())
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);
        emailService.enviar(usuario.getEmail(), "Bem-vindo(a) " + usuario.getNome());
        return UsuarioResponse.de(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id,AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        usuario.atualizarDados(request.nome().trim(), request.email().trim().toLowerCase());

        if (request.senha() != null) {
            String senhaCriptografada =passwordEncoder.encode(request.senha());
            usuario.alterarSenha(senhaCriptografada);
        }

        Usuario usuarioAtualizado = usuarioRepository.saveAndFlush(usuario);

        emailService.enviar(usuarioAtualizado.getEmail(), "Dados atualizados com sucesso.");

        return UsuarioResponse.de(usuarioAtualizado);
    }
    
}
