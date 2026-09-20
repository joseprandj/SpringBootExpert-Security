package io.github.joseprandj.SpringBootExpert_Security.service;


import io.github.joseprandj.SpringBootExpert_Security.repository.GrupoRepository;
import io.github.joseprandj.SpringBootExpert_Security.repository.UsuarioGrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioGrupoService {
    private final UsuarioGrupoRepository usuarioGrupoRepository;
}
