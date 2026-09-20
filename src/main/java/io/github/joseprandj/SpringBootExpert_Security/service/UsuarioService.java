package io.github.joseprandj.SpringBootExpert_Security.service;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Grupo;
import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;
import io.github.joseprandj.SpringBootExpert_Security.domain.entity.UsuarioGrupo;
import io.github.joseprandj.SpringBootExpert_Security.repository.GrupoRepository;
import io.github.joseprandj.SpringBootExpert_Security.repository.UsuarioGrupoRepository;
import io.github.joseprandj.SpringBootExpert_Security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario salvar(Usuario usuario, List<String> grupos) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);

        List<UsuarioGrupo> listaUsuarioGrupo = grupos
            .stream()
            .map(nomeGrupo -> {
                Optional<Grupo> possivelGrupo = grupoRepository.findByNome(nomeGrupo);

                if (possivelGrupo.isPresent()) {
                    Grupo grupo = possivelGrupo.get();
                    return new UsuarioGrupo(usuario, grupo);
                }

                return null;
            })
            .filter(Objects::nonNull)
            .toList();

        usuarioGrupoRepository.saveAll(listaUsuarioGrupo);

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario obterUsuarioComPermissoes(String login) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByLogin(login);
        if (usuarioOptional.isEmpty()) return null;

        Usuario usuario = usuarioOptional.get();
        List<String> permissoes = usuarioGrupoRepository.findPermissoesByUsuario(usuario);
        usuario.setPermissoes(permissoes);

        return usuario;
    };


}
