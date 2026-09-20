package io.github.joseprandj.SpringBootExpert_Security.service;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Grupo;
import io.github.joseprandj.SpringBootExpert_Security.repository.GrupoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {
    private final GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<Grupo> findAll() {
        return grupoRepository.findAll();
    }

    @Transactional
    public Grupo save(Grupo grupo) {
        grupo.setNome(grupo.getNome());
        return grupoRepository.save(grupo);
    }

}
