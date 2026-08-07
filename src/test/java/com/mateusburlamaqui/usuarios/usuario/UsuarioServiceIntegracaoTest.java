package com.mateusburlamaqui.usuarios.usuario;

import com.mateusburlamaqui.usuarios.email.EmailService;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class UsuarioServiceIntegracaoTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveRealizarRollbackQuandoEnvioDeEmailFalhar() {
        UsuarioRequest request = new UsuarioRequest("Mateus Burlamaqui", "mateus@gmail.com", "senha123");

        doThrow(new RuntimeException("Falha ao enviar e-mail")).when(emailService).enviar(anyString(), anyString());

        assertThrows(RuntimeException.class, () -> usuarioService.cadastrar(request));

        assertEquals(0, usuarioRepository.count());
    }
}
