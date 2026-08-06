package com.mateusburlamaqui.usuarios.usuario;

import com.mateusburlamaqui.usuarios.documentacao.OpenApiConfig;
import com.mateusburlamaqui.usuarios.excecao.ErroResponse;
import com.mateusburlamaqui.usuarios.usuario.dto.AtualizarUsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Cadastro, consulta e atualização de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastrar usuário", description = "Cadastra um novo usuário sem exigir autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response =usuarioService.cadastrar(request);
    
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar usuário por ID")
    @SecurityRequirement(name = OpenApiConfig.SEGURANCA_BASIC)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id) {
        UsuarioResponse response =usuarioService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar usuários", description = "Lista usuários com paginação e filtro opcional por parte do nome.")
    @SecurityRequirement(name = OpenApiConfig.SEGURANCA_BASIC)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada retornada"),
            @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas")
    })
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(@Parameter(description = "Trecho do nome para filtrar", example = "Mateus") @RequestParam(required = false) String nome,@ParameterObject Pageable pageable) {
        Page<UsuarioResponse> response = usuarioService.listar(nome, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza nome e e-mail. A senha é alterada somente quando informada.")
    @SecurityRequirement(name = OpenApiConfig.SEGURANCA_BASIC)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,@Valid @RequestBody AtualizarUsuarioRequest request) {
        UsuarioResponse response =usuarioService.atualizar(id, request);

        return ResponseEntity.ok(response);
    }
}

