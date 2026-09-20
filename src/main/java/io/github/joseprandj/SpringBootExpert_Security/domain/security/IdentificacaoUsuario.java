package io.github.joseprandj.SpringBootExpert_Security.domain.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentificacaoUsuario {
    private String id;
    private String nome;
    private String login;
    private List<String> permissoes;

    public List<String> getPermissoes() {
        if (permissoes == null) permissoes = new ArrayList<>();
        return permissoes;
    }
}
