package io.github.joseprandj.SpringBootExpert_Security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean // Responsável por registrar a cadeia de filtros de segurança no contexto do Sping
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        SenhaMasterAuthenticationProvider senhaMasterAuthenticationProvider,
        CustomAuthenticationProvider customAuthenticationProvider,
        CustomFilter customFilter
        ) throws Exception {
            return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer -> {
                    customizer.requestMatchers("/public").permitAll();
                    customizer.anyRequest().authenticated();
                })
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .authenticationProvider(senhaMasterAuthenticationProvider)
                .authenticationProvider(customAuthenticationProvider)
                .addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean // Responsável por carregar os dados de um usuário durante o processo de autenticação.
    public UserDetailsService userDetailsService() {
        UserDetails commonUser = User
            .builder()
            .username("user")
            .password(passwordEncoder().encode("123"))
            .roles("USER")
            .build();

        UserDetails admUser = User
            .builder()
            .username("admin")
            .password(passwordEncoder().encode("123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(commonUser, admUser);
    }

    @Bean // Responsável por realizar a verificação de senha
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
