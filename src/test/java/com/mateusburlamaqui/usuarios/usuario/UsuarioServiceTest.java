package com.mateusburlamaqui.usuarios.usuario;
import com.mateusburlamaqui.usuarios.email.EmailService;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}