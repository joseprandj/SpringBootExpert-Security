package io.github.joseprandj.SpringBootExpert_Security.domain.dto;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;

public record ResponseUsuarioDTO(String id, String nome) {

    public static ResponseUsuarioDTO responseDTO(Usuario usuario) {
        return new ResponseUsuarioDTO(usuario.getId(), usuario.getNome());
    }
}
