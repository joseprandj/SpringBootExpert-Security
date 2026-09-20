package io.github.joseprandj.SpringBootExpert_Security.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String login;
    private String senha;
    private String nome;

    @Transient // Utilizado para ignorar o mapeamento JPA, ou seja, não será persistido
    private List<String> permissoes;
}
