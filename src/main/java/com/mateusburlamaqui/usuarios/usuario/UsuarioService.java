package com.mateusburlamaqui.usuarios.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mateusburlamaqui.usuarios.email.EmailService;
import com.mateusburlamaqui.usuarios.usuario.excecao.EmailJaCadastradoException;
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

        String email = usuarioRequest.email().trim().toLowerCase();
        validarEmailDisponivel(email);

        Usuario usuario = new Usuario(
            usuarioRequest.nome().trim(),
            email,
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

        String email = request.email().trim().toLowerCase();
        validarEmailDisponivelParaAtualizacao(email, id);

        usuario.atualizarDados(request.nome().trim(), email);

        if (request.senha() != null) {
            String senhaCriptografada =passwordEncoder.encode(request.senha());
            usuario.alterarSenha(senhaCriptografada);
        }

        Usuario usuarioAtualizado = usuarioRepository.saveAndFlush(usuario);

        emailService.enviar(usuarioAtualizado.getEmail(), "Dados atualizados com sucesso.");

        return UsuarioResponse.de(usuarioAtualizado);
    }


    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String nome, Pageable pageable) {
        Page<Usuario> usuarios;

        if (nome == null || nome.isBlank()) {
            usuarios = usuarioRepository.findAll(pageable);
        } else {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        }

        return usuarios.map(usuario -> UsuarioResponse.de(usuario));
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailJaCadastradoException(email);
        }
    }

    private void validarEmailDisponivelParaAtualizacao(String email, Long id) {
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmailJaCadastradoException(email);
        }
    }
    
}
