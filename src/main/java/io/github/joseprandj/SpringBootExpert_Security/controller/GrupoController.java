package io.github.joseprandj.SpringBootExpert_Security.controller;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Grupo;
import io.github.joseprandj.SpringBootExpert_Security.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {
    private final GrupoService grupoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Grupo> salvar(@RequestBody Grupo requestGrupo) {
        Grupo grupo = grupoService.save(requestGrupo);
        return ResponseEntity.ok(grupo);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Grupo>> listarGrupos() {
        return ResponseEntity.ok(grupoService.findAll());
    }
}
