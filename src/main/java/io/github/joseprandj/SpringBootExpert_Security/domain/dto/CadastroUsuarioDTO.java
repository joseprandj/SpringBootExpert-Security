package io.github.joseprandj.SpringBootExpert_Security.domain.dto;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;

import java.util.List;

public record CadastroUsuarioDTO (Usuario usuario, List<String> permissoes) {

}
