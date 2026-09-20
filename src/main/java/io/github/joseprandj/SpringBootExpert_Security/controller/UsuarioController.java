package io.github.joseprandj.SpringBootExpert_Security.controller;


import io.github.joseprandj.SpringBootExpert_Security.domain.dto.CadastroUsuarioDTO;
import io.github.joseprandj.SpringBootExpert_Security.domain.dto.ResponseUsuarioDTO;
import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;
import io.github.joseprandj.SpringBootExpert_Security.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<ResponseUsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(
            usuarioService.findAll()
            .stream()
            .map(ResponseUsuarioDTO::responseDTO)
            .toList()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> salvar(@RequestBody CadastroUsuarioDTO body) {
        Usuario usuarioSalvo = usuarioService.salvar(body.usuario(), body.permissoes());
        return ResponseEntity.ok(usuarioSalvo);
    }
}
