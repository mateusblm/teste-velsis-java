package com.mateusburlamaqui.usuarios.seguranca;

import com.mateusburlamaqui.usuarios.usuario.Usuario;
import com.mateusburlamaqui.usuarios.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void devePermitirCadastroSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Mateus Burlamaqui",
                                    "email": "mateus@gmail.com",
                                    "senha": "senha123"
                                }
                                """)
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Mateus Burlamaqui"))
                .andExpect(jsonPath("$.email").value("mateus@gmail.com"));
    }

    @Test
    void deveNegarConsultaSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirConsultaComCredenciaisValidas()throws Exception {

        Usuario usuario = salvarUsuario();

        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId())
                        .with(httpBasic(
                                "mateus@gmail.com",
                                "senha123"
                        ))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Mateus Burlamaqui"))
                .andExpect(jsonPath("$.email").value("mateus@gmail.com"));
    }

    private Usuario salvarUsuario() {
        Usuario usuario = new Usuario("Mateus Burlamaqui","mateus@gmail.com",passwordEncoder.encode("senha123"));
        return usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void deveNegarConsultaComSenhaIncorreta() throws Exception {
        Usuario usuario = salvarUsuario();
        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId())
                        .with(httpBasic(
                                "mateus@gmail.com",
                                "senha-incorreta"
                        ))
                ).andExpect(status().isUnauthorized());
    }
}