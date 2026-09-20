# Spring Boot Expert — Security

Projeto desenvolvido para estudo dos principais conceitos e mecanismos de segurança utilizando **Spring Security** com **Spring Boot**.

O objetivo deste README é servir como material de consulta e revisão dos conceitos estudados, relacionando-os com os mecanismos utilizados na implementação do projeto.

---

## Sumário

1. [Fundamentos do Spring Security](#1-fundamentos-do-spring-security)
2. [SecurityFilterChain](#2-securityfilterchain)
3. [Autenticação x Autorização](#3-autenticação-x-autorização)
4. [AuthenticationManager e ProviderManager](#4-authenticationmanager-e-providermanager)
5. [Autenticação com usuário e senha](#5-autenticação-com-usuário-e-senha)
6. [Autenticação personalizada](#6-autenticação-personalizada)
7. [Authentication e SecurityContext](#7-authentication-e-securitycontext)
8. [Autorização](#8-autorização)
9. [Persistência de usuários e permissões](#9-persistência-de-usuários-e-permissões)
10. [Fluxos de autenticação](#10-fluxos-de-autenticação)
11. [Conceitos importantes](#11-conceitos-importantes)
12. [Pontos de atenção](#12-pontos-de-atenção)

---

# 1. Fundamentos do Spring Security

O **Spring Security** é o framework utilizado para implementar mecanismos de segurança em aplicações Spring.

Entre suas principais responsabilidades estão:

- autenticação de usuários;
- autorização de acesso aos recursos;
- processamento das requisições;
- gerenciamento das informações de autenticação;
- aplicação das regras de segurança;
- integração de diferentes mecanismos de autenticação.

Um dos conceitos fundamentais é que a segurança da aplicação não acontece em um único ponto.

A requisição passa por uma sequência de componentes responsáveis por analisar e processar as informações necessárias para determinar se aquela requisição pode continuar.

Essa estrutura é baseada principalmente na **cadeia de filtros de segurança**.

Ao adicionar o `spring-boot-starter-security`, o Spring Boot já protege todas as rotas com uma configuração padrão. No projeto, esse comportamento é substituído pela classe `SecurityConfig`, onde definimos as rotas públicas, os mecanismos de login, os providers de autenticação e o filtro personalizado.

O projeto implementa **quatro formas de autenticação** convivendo na mesma aplicação:

| Mecanismo | Como acessar | Componente |
|---|---|---|
| Usuários em memória | `user` e `admin` (senha `123`) | `UserDetailsService` + `PasswordEncoder` |
| Senha master | `master` / `123` | `SenhaMasterAuthenticationProvider` |
| Usuários do banco | login e senha cadastrados | `CustomAuthenticationProvider` |
| Header secreto | header `x-secret` | `CustomFilter` |

---

# 2. SecurityFilterChain

A `SecurityFilterChain` representa a cadeia de processamento pela qual as requisições passam antes de chegar aos recursos protegidos da aplicação.

É nela que configuramos parte importante do comportamento do Spring Security.

No projeto, ela também é responsável por integrar os mecanismos de autenticação utilizados.

Entre as configurações utilizadas estão:

- regras de acesso por rota (`/public` liberada e as demais exigindo autenticação);
- habilitação do login por formulário e por HTTP Basic;
- desabilitação do CSRF, para facilitar o teste da API;
- registro dos providers personalizados;
- inclusão do filtro personalizado;
- definição da posição desse filtro dentro da cadeia de filtros.

A configuração:

```
.authenticationProvider(senhaMasterAuthenticationProvider)
.authenticationProvider(customAuthenticationProvider)
```

registra os `AuthenticationProvider` personalizados para que eles possam participar do processo de autenticação.

Já:

```
.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
```

adiciona o filtro personalizado antes do `UsernamePasswordAuthenticationFilter`.

A posição do filtro é importante porque a ordem dos filtros determina quando determinado mecanismo de segurança será executado durante o processamento da requisição.

### Por que a ordem dos filtros importa?

Uma requisição passa por vários filtros em uma determinada sequência.

Portanto, adicionar um filtro em uma posição específica permite determinar em que momento ele deve analisar ou modificar o contexto da requisição.

No projeto, o filtro personalizado precisa ser executado antes do filtro responsável pelo processamento tradicional de usuário e senha.

### Regras de acesso por rota

As regras de `authorizeHttpRequests` são avaliadas na ordem em que foram declaradas:

```
.requestMatchers("/public").permitAll()
.anyRequest().authenticated()
```

Por isso a regra genérica `anyRequest()` deve ser sempre a última.

---

# 3. Autenticação x Autorização

Embora estejam relacionadas, autenticação e autorização representam conceitos diferentes.

## Autenticação

A autenticação responde:
> **Quem é você?**

É o processo responsável por verificar uma identidade apresentada à aplicação.

No projeto, existem diferentes formas de apresentar essa identidade, incluindo:

- usuário e senha em memória;
- usuário e senha `master`;
- usuário e senha cadastrados no banco;
- header `x-secret`.

---

## Autorização

A autorização responde:
> **O que você pode fazer?**

Depois que uma identidade é autenticada, a aplicação utiliza as **authorities** dessa autenticação para determinar quais recursos ou operações podem ser acessados.

Portanto:

**Autenticação → identifica o usuário**

**Autorização → determina o que ele pode acessar**

---

# 4. AuthenticationManager e ProviderManager

Os filtros de login (formulário e HTTP Basic) não validam as credenciais sozinhos.

Eles recebem o login e a senha, montam um `UsernamePasswordAuthenticationToken` e entregam ao `AuthenticationManager`.

A implementação padrão é o **`ProviderManager`**, que consulta os providers registrados, na ordem, seguindo esta lógica:

- `supports()` indica se o provider sabe tratar aquele tipo de autenticação;
- se `authenticate()` retorna um `Authentication`, a autenticação foi concluída;
- se `authenticate()` retorna `null`, significa "não é comigo" e o próximo provider é consultado;
- se todos retornarem `null`, o `ProviderManager` consulta o seu `parent`.

No projeto, a cadeia é:

```
ProviderManager
      │
      ├── 1. SenhaMasterAuthenticationProvider     (master / 123)
      ├── 2. CustomAuthenticationProvider          (usuários do banco)
      │
      ▼
   parent
      │
      └── DaoAuthenticationProvider                (usuários em memória)
```

Os dois providers personalizados são registrados pelo `.authenticationProvider(...)`, na ordem em que aparecem na `SecurityFilterChain`.

O `DaoAuthenticationProvider` não aparece na configuração: o Spring Security o cria automaticamente a partir do bean `UserDetailsService`.

É por isso que os usuários em memória continuam funcionando mesmo com providers personalizados registrados antes deles.

---

# 5. Autenticação com usuário e senha

A autenticação tradicional utilizando usuário e senha é um dos mecanismos estudados no projeto.

Nesse fluxo, a aplicação precisa:

1. receber as credenciais;
2. localizar as informações do usuário;
3. obter os dados necessários para validar a senha;
4. verificar se a senha informada corresponde à armazenada;
5. estabelecer a autenticação caso as credenciais sejam válidas.

Dois componentes importantes nesse processo são:

- `UserDetailsService`;
- `PasswordEncoder`.

---

## UserDetailsService

O `UserDetailsService` é responsável por fornecer as informações do usuário utilizadas durante a autenticação.

Sua principal responsabilidade é localizar o usuário e disponibilizar suas informações através de um `UserDetails`.

É importante perceber que o `UserDetailsService` **não é responsável por validar a senha**.

No projeto, ele é uma implementação em memória (`InMemoryUserDetailsManager`) com dois usuários: `user` (perfil `USER`) e `admin` (perfil `ADMIN`).

---

## PasswordEncoder

O `PasswordEncoder` é utilizado para trabalhar com as senhas de maneira segura.

Ele possui duas responsabilidades conceituais importantes:

- codificar uma senha;
- verificar se uma senha informada corresponde à senha codificada armazenada.

No projeto é utilizado o **BCrypt** (`BCryptPasswordEncoder`), aproveitado em três pontos:

- na criação dos usuários em memória;
- no cadastro de usuários no banco (`UsuarioService`);
- na verificação de senha do `CustomAuthenticationProvider`.

A aplicação nunca armazena a senha em texto puro: o banco guarda apenas o hash.

---

## Separação de responsabilidades

**UserDetailsService**

→ fornece os dados do usuário.

**PasswordEncoder**

→ realiza o tratamento e a verificação da senha.

**DaoAuthenticationProvider**

→ une os dois: busca o usuário e valida a senha.

---

# 6. Autenticação personalizada

Além da autenticação tradicional, o projeto implementa três mecanismos personalizados:

- `SenhaMasterAuthenticationProvider`;
- `CustomAuthenticationProvider`;
- `CustomFilter`.

Os dois primeiros usam o ponto de extensão **`AuthenticationProvider`**. O terceiro usa um **filtro**.

---

## AuthenticationProvider

O `AuthenticationProvider` representa o componente responsável por realizar a lógica de autenticação de um determinado tipo de credencial.

Ele possui dois métodos:

- `supports()`: indica quais tipos de autenticação o provider sabe processar;
- `authenticate()`: executa a lógica de autenticação. Retorna um `Authentication` em caso de sucesso ou `null` para deixar o próximo provider tentar.

Nos providers do projeto, `supports()` retorna sempre `true`.

---

## SenhaMasterAuthenticationProvider

Autentica um acesso fixo definido no código: login `master` e senha `123`.

Quando as credenciais conferem, devolve um `Authentication` com a authority `ADMIN`. Para qualquer outro login, retorna `null` e o próximo provider é consultado.

**O que resolve:** o banco sobe vazio, então não existe nenhum usuário administrador cadastrado. O acesso `master` permite criar os primeiros grupos e usuários.

---

## CustomAuthenticationProvider

Autentica os usuários armazenados no banco de dados.

Seu funcionamento:

1. lê o login e a senha informados;
2. busca o usuário no banco já com as suas permissões;
3. compara a senha com o hash usando o `PasswordEncoder`;
4. se conferir, devolve uma `CustomAuthentication` com a identificação do usuário;
5. caso contrário, retorna `null`.

**O que resolve:** usuários e permissões passam a ser dados da aplicação, e não valores fixos no código.

---

## CustomFilter

O filtro personalizado (`CustomFilter`) participa diretamente do processamento da requisição HTTP.

Ele lê o header `x-secret` e, se o valor for válido, cria uma autenticação com a authority `USER` e a grava **diretamente** no `SecurityContextHolder`.

Pontos importantes:

- **não passa pelo `AuthenticationManager`**: nenhum provider participa desse fluxo;
- a autenticação vale apenas para aquela requisição, então o header deve ser enviado em toda chamada;
- se o header não existir ou for inválido, a requisição segue a cadeia sem autenticação.

**O que resolve:** demonstra o padrão de autenticação por credencial enviada no header (como API key ou token): ler a credencial, validar e preencher o `SecurityContext`.

---

## Filtro x Provider

O filtro e os providers **não são etapas do mesmo fluxo**. São formas independentes de autenticar.

O filtro está mais próximo da **requisição HTTP**: lê o header e preenche o contexto.

O provider está mais próximo da **validação de credenciais**: é chamado pelo `ProviderManager` quando chega um login e senha.

---

# 7. Authentication e SecurityContext

A interface `Authentication` representa o resultado da autenticação.

Ela reúne:

- o **principal** (quem é o usuário);
- as **authorities** (o que ele pode fazer);
- se a autenticação já foi validada.

No projeto existem duas formas de resultado:

- `UsernamePasswordAuthenticationToken`: usado pelo provider master, pelo filtro e pelos usuários em memória;
- `CustomAuthentication`: implementação própria, usada pelo `CustomAuthenticationProvider`.

A `CustomAuthentication` carrega uma `IdentificacaoUsuario` como principal (id, nome, login e permissões) e transforma cada permissão em uma authority.

**O que resolve:** permite recuperar, em qualquer ponto da aplicação, mais informações do usuário logado do que apenas o nome.

## SecurityContextHolder

O `SecurityContextHolder` guarda a autenticação da requisição em andamento.

- **quem escreve:** os filtros de login (após a autenticação) e o `CustomFilter`;
- **quem lê:** a autorização por rota, a autorização por método e os controllers que recebem `Authentication`.

---

# 8. Autorização

No projeto, a autorização acontece em duas camadas:

- **por rota**, na `SecurityFilterChain`: `/public` liberada e as demais exigindo autenticação;
- **por método**, com `@PreAuthorize`, habilitado por `@EnableMethodSecurity`.

As regras por método usam `hasRole` e `hasAnyRole`:

```
@PreAuthorize("hasAnyRole('GERENTE_RH', 'ADMIN')")
```

O projeto não usa hierarquia de perfis, por isso cada regra lista todos os perfis aceitos.

## O prefixo ROLE_

Por padrão, `hasRole('ADMIN')` procura a authority `ROLE_ADMIN`.

O projeto altera esse comportamento com:

```
new GrantedAuthorityDefaults("")
```

Com o prefixo vazio, `hasRole('ADMIN')` procura a authority `ADMIN`.

**O que resolve:** os nomes dos grupos gravados no banco (`ADMIN`, `GERENTE_RH`, `TECNICO_RH`) são usados diretamente como authorities.

## Endpoints e regras

| Endpoint | Regra |
|---|---|
| `GET /public` | liberado |
| `GET /private` | qualquer usuário autenticado |
| `GET /admin` | `ADMIN` |
| `GET /rh/tecnico` | `TECNICO_RH`, `GERENTE_RH` ou `ADMIN` |
| `GET /rh/gerente` | `GERENTE_RH` ou `ADMIN` |
| `GET /usuarios` | qualquer usuário autenticado |
| `POST /usuarios` | `ADMIN` |
| `GET /grupos` e `POST /grupos` | `ADMIN` |

Sem autenticação, a rota protegida responde `401`. Autenticado sem a authority exigida, responde `403`.

---

# 9. Persistência de usuários e permissões

Os usuários autenticados pelo `CustomAuthenticationProvider` vêm do banco (H2 em memória), modelado com três entidades:

- **`Usuario`**: login, senha (hash BCrypt) e nome;
- **`Grupo`**: representa uma permissão. O **nome do grupo** é a authority;
- **`UsuarioGrupo`**: liga usuários e grupos (relacionamento N:N).

O campo `permissoes` de `Usuario` é `@Transient`: não é persistido e serve apenas para carregar os grupos do usuário durante a autenticação.

**Login:** o `UsuarioService` busca o usuário por login e carrega os nomes dos grupos por uma consulta no `UsuarioGrupoRepository`.

**Cadastro:** o `UsuarioService` codifica a senha, salva o usuário e cria os vínculos com os grupos informados. Grupos que não existem são ignorados, então devem ser criados antes.

Os DTOs separam o que entra e o que sai da API:

- `CadastroUsuarioDTO`: dados do usuário e nomes dos grupos;
- `ResponseUsuarioDTO`: retorna apenas `id` e `nome`, sem a senha.

---

# 10. Fluxos de autenticação

## Login e senha (formulário ou Basic)

```
Requisição com login e senha
   ↓
ProviderManager
   ↓
SenhaMasterAuthenticationProvider
   ↓ (null)
CustomAuthenticationProvider
   ↓ (null)
DaoAuthenticationProvider (usuários em memória)
   ↓
Autenticação
```

## Header x-secret

```
Requisição
   ↓
CustomFilter
   ↓
Header válido?
   ↓
SecurityContextHolder
   ↓
Autenticação
```

## Usuário do banco

```
Login e senha
   ↓
CustomAuthenticationProvider
   ↓
Busca do usuário e das permissões
   ↓
Verificação da senha (PasswordEncoder)
   ↓
CustomAuthentication
```

---

# 11. Conceitos importantes

## Separação de responsabilidades

Um dos principais conceitos observados no projeto é a divisão das responsabilidades entre os componentes.

Cada elemento participa de uma parte específica do processo.

Por exemplo:

- `SecurityFilterChain` → organiza os componentes dentro do fluxo de segurança;
- `CustomFilter` → lê a credencial do header e preenche o contexto;
- `ProviderManager` → coordena os providers;
- `AuthenticationProvider` → executa a lógica específica de autenticação;
- `UserDetailsService` → fornece informações do usuário;
- `PasswordEncoder` → trabalha com a verificação da senha;
- `@PreAuthorize` → aplica as regras de acesso por método.

---

## Ordem dos filtros e dos providers

A ordem dos filtros não é apenas uma questão de organização. Ela influencia diretamente o comportamento do processo de segurança.

O mesmo vale para os providers: o `ProviderManager` os consulta em sequência e o primeiro que devolver uma autenticação encerra o processo.

---

## Diferentes mecanismos de autenticação

O Spring Security não está limitado à autenticação tradicional com usuário e senha.

É possível implementar diferentes mecanismos de autenticação, desde que eles sejam integrados corretamente ao fluxo de segurança.

O projeto demonstra isso através da coexistência de:

- autenticação tradicional (usuários em memória);
- autenticação por provider (master e usuários do banco);
- autenticação por filtro (header `x-secret`).

---

## Autenticação não é autorização

A autenticação estabelece uma identidade.

A autorização utiliza essa identidade para determinar quais operações ou recursos podem ser acessados.

No projeto, os grupos do banco viram authorities na autenticação, e o `@PreAuthorize` as utiliza para liberar ou bloquear cada endpoint.

---

## Visão geral do projeto

```
                     Spring Security
                           │
                           ▼
                  SecurityFilterChain
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
        ▼                                     ▼
  CustomFilter                        Filtros de login
  (header x-secret)                (formulário / Basic)
        │                                     │
        │                                     ▼
        │                            ProviderManager
        │                                     │
        │               ┌─────────────────────┼─────────────────────┐
        │               ▼                     ▼                     ▼
        │      SenhaMaster...Provider  CustomAuthentication...  DaoAuthentication...
        │                                                    (UserDetailsService)
        │               │                     │                     │
        └───────────────┴──────────┬──────────┴─────────────────────┘
                                   ▼
                          SecurityContextHolder
                                   │
                                   ▼
                              Autorização
                       (rota + @PreAuthorize)
```

---

# 12. Pontos de atenção

Observações levantadas ao revisar o código. Como o projeto é de estudo, servem de referência para uma aplicação real.

- **Usuários em memória e `ROLE_`:** `.roles("ADMIN")` grava a authority `ROLE_ADMIN`, mas com o prefixo vazio o `hasRole('ADMIN')` procura `ADMIN`. O `admin` em memória autentica, mas não passa nas rotas de `ADMIN`. Solução: usar `.authorities("ADMIN")`.
- **`IdentificacaoUsuario`:** o `CustomAuthenticationProvider` passa `login` e `nome` invertidos no construtor, então `/private` exibe o login no lugar do nome.
- **`supports()`:** retorna sempre `true`. O ideal é restringir ao tipo de autenticação processado.
- **Senha incorreta:** o `CustomAuthenticationProvider` retorna `null`; lançar `BadCredentialsException` deixaria a intenção explícita.
- **`POST /usuarios`:** devolve a entidade `Usuario`, incluindo o hash da senha. O `GET` já usa o `ResponseUsuarioDTO` para evitar isso.
- **Simplificações do estudo:** credenciais fixas no código, CSRF desabilitado e banco sem dados iniciais.
