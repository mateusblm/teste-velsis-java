package com.mateusburlamaqui.usuarios.usuario;
import com.mateusburlamaqui.usuarios.email.EmailService;
import com.mateusburlamaqui.usuarios.usuario.excecao.EmailJaCadastradoException;
import com.mateusburlamaqui.usuarios.usuario.dto.AtualizarUsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuario() {
        UsuarioRequest request = new UsuarioRequest( "Mateus Burlamaqui", "mateus@gmail.com", "senha123");

        when(passwordEncoder.encode(request.senha())).thenReturn("senha-criptografada");

        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioResponse response = usuarioService.cadastrar(request);

        assertEquals("Mateus Burlamaqui", response.nome());
        assertEquals("mateus@gmail.com", response.email());

        verify(passwordEncoder).encode("senha123");
        // Garante que a senha original não seja enviada ao repositório
        verify(usuarioRepository).saveAndFlush(argThat(usuario ->"senha-criptografada".equals(usuario.getSenha())));

        verify(emailService).enviar(eq("mateus@gmail.com"), anyString());    
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = new Usuario( "Mateus Burlamaqui","mateus@gmail.com","senha-criptografada");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.buscarPorId(1L);

        assertEquals("Mateus Burlamaqui", response.nome());
        assertEquals("mateus@gmail.com", response.email());

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        UsuarioNaoEncontradoException excecao = assertThrows(UsuarioNaoEncontradoException.class,() -> usuarioService.buscarPorId(99L));

        assertEquals("Usuário não encontrado com o ID: 99",excecao.getMessage());

        verify(usuarioRepository).findById(99L);
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenha() {
        Usuario usuario = new Usuario( "Mateus", "mateus-antigo@gmail.com", "senha-antiga-criptografada");

        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Mateus Burlamaqui", "mateus-novo@gmail.com", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        when(usuarioRepository.saveAndFlush(usuario)).thenReturn(usuario);

        UsuarioResponse response =usuarioService.atualizar(1L, request);

        assertEquals("Mateus Burlamaqui", response.nome());
        assertEquals("mateus-novo@gmail.com", response.email());
        assertEquals("senha-antiga-criptografada",usuario.getSenha());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).saveAndFlush(usuario);

        verify(passwordEncoder, never()).encode(anyString());

        verify(emailService).enviar(eq("mateus-novo@gmail.com"),anyString());
    }

    @Test
    void deveAtualizarUsuarioComNovaSenha() {
        Usuario usuario = new Usuario( "Mateus", "mateus-antigo@gmail.com", "senha-antiga-criptografada");

        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Mateus Burlamaqui", "mateus-novo@gmail.com", "novaSenha123");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("novaSenha123")).thenReturn("nova-senha-criptografada");

        when(usuarioRepository.saveAndFlush(usuario)).thenReturn(usuario);

        UsuarioResponse response = usuarioService.atualizar(1L, request);

        assertEquals("Mateus Burlamaqui", response.nome());
        assertEquals("mateus-novo@gmail.com", response.email());
        assertEquals("nova-senha-criptografada",usuario.getSenha());

        verify(passwordEncoder).encode("novaSenha123");

        verify(usuarioRepository).saveAndFlush(usuario);

        verify(emailService).enviar(eq("mateus-novo@gmail.com"), anyString());
    }

    @Test
    void deveLancarErroAoAtualizarUsuarioInexistente() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Mateus Burlamaqui","mateus@gmail.com",null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        UsuarioNaoEncontradoException excecao = assertThrows(UsuarioNaoEncontradoException.class,() -> usuarioService.atualizar(1L, request));

        assertEquals("Usuário não encontrado com o ID: 1", excecao.getMessage());

        verify(usuarioRepository).findById(1L);

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));

        verify(passwordEncoder, never()).encode(anyString());

        verify(emailService, never()).enviar(anyString(), anyString());
    }


    @Test
    void deveListarUsuariosSemFiltro() {
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = new Usuario("Mateus Burlamaqui","mateus@gmail.com","senha-criptografada");

        Page<Usuario> pagina = new PageImpl<>(List.of(usuario), pageable, 1);

        when(usuarioRepository.findAll(pageable)).thenReturn(pagina);

        Page<UsuarioResponse> resultado = usuarioService.listar(null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mateus Burlamaqui", resultado.getContent().getFirst().nome());
        assertEquals("mateus@gmail.com", resultado.getContent().getFirst().email());

        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    void deveListarUsuariosFiltrandoPorNome() {
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = new Usuario("Mateus Burlamaqui", "mateus@gmail.com", "senha-criptografada");

        Page<Usuario> pagina = new PageImpl<>(List.of(usuario), pageable, 1);

        when(usuarioRepository.findByNomeContainingIgnoreCase("Mateus", pageable)).thenReturn(pagina);

        Page<UsuarioResponse> resultado =usuarioService.listar("  Mateus  ", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mateus Burlamaqui", resultado.getContent().getFirst().nome());

        verify(usuarioRepository).findByNomeContainingIgnoreCase( "Mateus", pageable);
    }


    @Test
    void deveImpedirCadastroComEmailJaCadastrado() {
        UsuarioRequest request = new UsuarioRequest("Mateus Burlamaqui", "mateus@gmail.com", "senha123");

        when(usuarioRepository.existsByEmailIgnoreCase("mateus@gmail.com")).thenReturn(true);

        EmailJaCadastradoException excecao = assertThrows(EmailJaCadastradoException.class, () -> usuarioService.cadastrar(request));

        assertEquals("Já existe um usuário cadastrado com o e-mail: "+ "mateus@gmail.com", excecao.getMessage());

        verify(passwordEncoder, never()).encode(anyString());

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));

        verify(emailService, never()).enviar(anyString(), anyString());
    }

    @Test
    void deveImpedirAtualizacaoComEmailDeOutroUsuario() {
        Usuario usuario = new Usuario("Mateus","email-antigo@gmail.com","senha-criptografada");

        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Mateus Burlamaqui", "email-existente@gmail.com", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("email-existente@gmail.com", 1L)).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class, () -> usuarioService.atualizar(1L, request));

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));

        verify(emailService, never()).enviar(anyString(), anyString());
    }
}
