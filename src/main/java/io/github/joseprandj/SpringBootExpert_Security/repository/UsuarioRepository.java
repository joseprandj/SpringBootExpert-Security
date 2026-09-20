package io.github.joseprandj.SpringBootExpert_Security.repository;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    public Optional<Usuario> findByLogin(String login);
}
