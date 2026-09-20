package io.github.joseprandj.SpringBootExpert_Security.config;

import io.github.joseprandj.SpringBootExpert_Security.domain.entity.Usuario;
import io.github.joseprandj.SpringBootExpert_Security.domain.security.CustomAuthentication;
import io.github.joseprandj.SpringBootExpert_Security.domain.security.IdentificacaoUsuario;
import io.github.joseprandj.SpringBootExpert_Security.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String senha = authentication.getCredentials().toString();

        Usuario usuario = usuarioService.obterUsuarioComPermissoes(login);
        if (usuario != null) {
            boolean senhaBatem = passwordEncoder.matches(senha, usuario.getSenha());
            if (senhaBatem) {
                IdentificacaoUsuario identificacaoUsuario = new IdentificacaoUsuario(
                    usuario.getId(),
                    usuario.getLogin(),
                    usuario.getNome(),
                    usuario.getPermissoes()
                );

                return new CustomAuthentication(identificacaoUsuario);
            }
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
