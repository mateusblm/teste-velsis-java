package com.mateusburlamaqui.usuarios.usuario;

import com.mateusburlamaqui.usuarios.usuario.dto.AtualizarUsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioRequest;
import com.mateusburlamaqui.usuarios.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
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
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response =usuarioService.cadastrar(request);
    
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        UsuarioResponse response =usuarioService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(@RequestParam(required = false) String nome,Pageable pageable) {
        Page<UsuarioResponse> response = usuarioService.listar(nome, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,@Valid @RequestBody AtualizarUsuarioRequest request) {
        UsuarioResponse response =usuarioService.atualizar(id, request);

        return ResponseEntity.ok(response);
    }
}