# SpringBoot - Security


```
                      ┌──────────────────────────┐
                      │   Security Filter Chain  │
                      │                          │
                      │       Autenticação       │
                      │  (Validação de acesso)   │
                      └────────────┬─────────────┘
                                   │
                                   │ Requisição autenticada
                                   ▼
┌──────────────┐             ┌──────────────┐
│              │  Request    │              │
│    Client    ├────────────►│     API      │
│              │             │              │
└──────▲───────┘             └──────┬───────┘
       │                            │
       │         Response           │
       └────────────────────────────┘
```

Por padrão, ao adicionar a dependência do Spring Security ao `pom.xml`, o Spring Boot realiza a autoconfiguração de segurança, disponibilizando componentes e regras de proteção padrão, como autenticação das requisições HTTP. Dessa forma, a aplicação já inicia com uma camada básica de segurança, que pode ser personalizada conforme as necessidades do projeto.
  - Configurações aplicadas automaticamente
    - **Proteção dos endpoints:** Requisições HTTP exigem autenticação, salvo configurações específicas que permitam acesso público.
    - **Autenticação padrão:** Incluindo um usuário(user)padrão gerado e uma senha aleatória exibida nos logs, quando aplicável.
    - **Filtros de segurança:** Utiliza uma cadeia de filtros para interceptar requisições e executar processos como autenticação, autorização e proteção contra determinados ataques.
  - Possibilidade de personalização
    - Pode substituir ou ajustar as configurações automáticas utilizando `Beans`, como `SecurityFilterChain`, e outros componentes de segurança.
---

# Configuração do SecurityFilterChain

Ao criar um Bean do tipo `SecurityFilterChain`, passamos a definir explicitamente as regras de segurança da aplicação. Essa configuração faz com que o Spring Boot deixe de aplicar sua configuração padrão de segurança baseada em auto configuração, permitindo que a aplicação determine como as requisições serão protegidas.

Dessa forma, é necessário configurar as regras de autorização e, conforme o mecanismo de autenticação escolhido, os componentes responsáveis por validar a identidade do usuário.

## 1. Autorização e autenticação
A autorização define quais recursos podem ser acessados e quais regras devem ser aplicadas às requisições.

```
.authorizeHttpRequests(customizer -> {
    customizer.requestMatchers("/public").permitAll();
    customizer.anyRequest().authenticated();
})
```

**Importante:** o método .authenticated() apenas exige que o usuário esteja autenticado. Ele não define, por si só, como a identidade do usuário será validada, nem de onde os dados do usuário serão obtidos.

## 2. UserDetailsService — Dados do usuário

O UserDetailsService é uma interface utilizada pelo Spring Security para carregar os dados de um usuário durante o processo de autenticação.

Sua responsabilidade é localizar o usuário, geralmente por meio de um nome de usuário (username), e retornar um objeto UserDetails contendo as informações necessárias para a autenticação e autorização.

Esses dados podem ser obtidos de diferentes fontes, como:
 - Banco de dados;
 - Serviço externo;
 - Usuários em memória, para cenários de desenvolvimento e testes.

**Importante:** o UserDetailsService é responsável por carregar os dados do usuário. A validação da senha é realizada pelo mecanismo de autenticação, utilizando um PasswordEncoder quando aplicável.

## 3. PasswordEncoder — Codificação e validação de senhas

O PasswordEncoder é responsável por realizar operações relacionadas à codificação e à verificação de senhas.

Suas principais operações são:
 - encode(): transforma uma senha em texto puro em uma representação codificada, adequada para armazenamento.
 - matches(): verifica se a senha informada pelo usuário corresponde à senha codificada armazenada.


**Importante:** as senhas não devem ser armazenadas em texto puro. O PasswordEncoder deve ser utilizado para armazenar e verificar as senhas de forma segura.