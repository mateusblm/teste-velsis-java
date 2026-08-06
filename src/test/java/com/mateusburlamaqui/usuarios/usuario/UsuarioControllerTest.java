package com.mateusburlamaqui.usuarios.usuario;

import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import com.mateusburlamaqui.usuarios.usuario.excecao.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuario() throws Exception {
        UsuarioResponse response = new UsuarioResponse(1L,"Mateus Burlamaqui","mateus@gmail.com");

        when(usuarioService.cadastrar(any(UsuarioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                .content("""
                        {
                        "nome": "Mateus Burlamaqui",
                        "email": "mateus@gmail.com",
                        "senha": "senha123"
                        }
                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mateus Burlamaqui"))
                .andExpect(jsonPath("$.email").value("mateus@gmail.com"));

        verify(usuarioService).cadastrar(any(UsuarioRequest.class));
    }


    @Test
    void deveRetornarErroQuandoDadosForemInvalidos()
            throws Exception {

        mockMvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                .content("""
                        {
                        "nome": "",
                        "email": "email-invalido",
                        "senha": "123"
                        }
                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."))
                .andExpect(jsonPath("$.campos.nome").value("O nome é obrigatório."))
                .andExpect(jsonPath("$.campos.email").value("O e-mail informado é inválido."))
                .andExpect(jsonPath("$.campos.senha").value("A senha deve ter entre 8 e 100 caracteres."));
    }

    @Test
    void deveRetornarErroQuandoUsuarioNaoExistir()throws Exception {

        when(usuarioService.buscarPorId(99L)).thenThrow(new UsuarioNaoEncontradoException(99L));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado com o ID: 99"));

        verify(usuarioService).buscarPorId(99L);
    }

    @Test
    void deveListarUsuarios() throws Exception {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Mateus Burlamaqui", "mateus@gmail.com");

        PageImpl<UsuarioResponse> pagina = new PageImpl<>(List.of(usuario));

        when(usuarioService.listar(isNull(), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/usuarios").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Mateus Burlamaqui"))
                .andExpect(jsonPath("$.content[0].email").value("mateus@gmail.com"));
    }
}