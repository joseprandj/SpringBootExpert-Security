# Spring Boot Expert — Security

Projeto desenvolvido para estudo dos principais conceitos e mecanismos de segurança utilizando **Spring Security** com **Spring Boot**.

O objetivo deste README é servir como material de consulta e revisão dos conceitos estudados, relacionando-os com os mecanismos utilizados na implementação do projeto.

---

## Sumário

1. [Fundamentos do Spring Security](#1-fundamentos-do-spring-security)
2. [SecurityFilterChain](#2-securityfilterchain)
3. [Autenticação x Autorização](#3-autenticação-x-autorização)
4. [Autenticação com usuário e senha](#4-autenticação-com-usuário-e-senha)
5. [Autenticação personalizada](#5-autenticação-personalizada)
6. [Integração entre filtro e AuthenticationProvider](#6-integração-entre-filtro-e-authenticationprovider)
7. [Fluxo de autenticação](#7-fluxo-de-autenticação)
8. [Conceitos importantes](#8-conceitos-importantes)

---

# 1. Fundamentos do Spring Security

O **Spring Security** é o framework utilizado para implementar mecanismos de segurança em aplicações Spring.

Entre suas principais responsabilidades estão:

* autenticação de usuários;
* autorização de acesso aos recursos;
* processamento das requisições;
* gerenciamento das informações de autenticação;
* aplicação das regras de segurança;
* integração de diferentes mecanismos de autenticação.

Um dos conceitos fundamentais é que a segurança da aplicação não acontece em um único ponto.

A requisição passa por uma sequência de componentes responsáveis por analisar e processar as informações necessárias para determinar se aquela requisição pode continuar.

Essa estrutura é baseada principalmente na **cadeia de filtros de segurança**.

---

# 2. SecurityFilterChain

A `SecurityFilterChain` representa a cadeia de processamento pela qual as requisições passam antes de chegar aos recursos protegidos da aplicação.

É nela que configuramos parte importante do comportamento do Spring Security.

No projeto, ela também é responsável por integrar os mecanismos de autenticação utilizados.

Entre as configurações utilizadas estão:

* registro do `SenhaMasterAuthenticationProvider`;
* inclusão do filtro personalizado;
* definição da posição desse filtro dentro da cadeia de filtros.

A configuração:

```java
.authenticationProvider(senhaMasterAuthenticationProvider)
```

registra o `AuthenticationProvider` personalizado para que ele possa participar do processo de autenticação.

Já:

```java
.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
```

adiciona o filtro personalizado antes do `UsernamePasswordAuthenticationFilter`.

A posição do filtro é importante porque a ordem dos filtros determina quando determinado mecanismo de segurança será executado durante o processamento da requisição.

### Por que a ordem dos filtros importa?

Uma requisição passa por vários filtros em uma determinada sequência.

Portanto, adicionar um filtro em uma posição específica permite determinar em que momento ele deve analisar ou modificar o contexto da requisição.

No projeto, o filtro personalizado precisa ser executado antes do filtro responsável pelo processamento tradicional de usuário e senha.

---

# 3. Autenticação x Autorização

Embora estejam relacionadas, autenticação e autorização representam conceitos diferentes.

## Autenticação

A autenticação responde:

> **Quem é você?**

É o processo responsável por verificar uma identidade apresentada à aplicação.

No projeto, existem diferentes formas de apresentar essa identidade, incluindo:

* usuário e senha;
* credencial utilizada pelo mecanismo de autenticação personalizada.

---

## Autorização

A autorização responde:

> **O que você pode fazer?**

Depois que uma identidade é autenticada, a aplicação pode utilizar as informações dessa autenticação para determinar quais recursos ou operações podem ser acessados.

Portanto:

**Autenticação → identifica o usuário**

**Autorização → determina o que ele pode acessar**

Uma aplicação pode possuir diferentes mecanismos de autenticação e utilizar o resultado deles para aplicar as regras de autorização.

---

# 4. Autenticação com usuário e senha

A autenticação tradicional utilizando usuário e senha é um dos mecanismos estudados no projeto.

Nesse fluxo, a aplicação precisa:

1. receber as credenciais;
2. localizar as informações do usuário;
3. obter os dados necessários para validar a senha;
4. verificar se a senha informada corresponde à armazenada;
5. estabelecer a autenticação caso as credenciais sejam válidas.

Dois componentes importantes nesse processo são:

* `UserDetailsService`;
* `PasswordEncoder`.

---

## UserDetailsService

O `UserDetailsService` é responsável por fornecer as informações do usuário utilizadas durante a autenticação.

Sua principal responsabilidade é localizar o usuário e disponibilizar suas informações através de um `UserDetails`.

O `UserDetails` representa os dados que o Spring Security precisa conhecer sobre aquele usuário para realizar o processo de autenticação e posteriormente trabalhar com autorização.

É importante perceber que o `UserDetailsService` **não é responsável por validar a senha**.

Sua responsabilidade principal é fornecer os dados do usuário.

---

## PasswordEncoder

O `PasswordEncoder` é utilizado para trabalhar com as senhas de maneira segura.

Ele possui duas responsabilidades conceituais importantes:

* codificar uma senha;
* verificar se uma senha informada corresponde à senha codificada armazenada.

A aplicação não deve tratar a senha armazenada como texto puro.

O objetivo é trabalhar com uma representação codificada da senha e utilizar o mecanismo apropriado para verificar uma tentativa de autenticação.

---

## Separação de responsabilidades

Nesse fluxo podemos perceber uma separação importante:

**UserDetailsService**

→ fornece os dados do usuário.

**PasswordEncoder**

→ realiza o tratamento e a verificação da senha.

Essa separação permite que cada componente tenha uma responsabilidade específica dentro do processo de autenticação.

---

# 5. Autenticação personalizada

Além da autenticação tradicional com usuário e senha, o projeto implementa um mecanismo de **autenticação personalizada**.

Nesse caso, o processo não depende exclusivamente do fluxo tradicional de usuário e senha.

A autenticação personalizada implementada no projeto utiliza dois elementos principais:

* um **filtro personalizado**;
* um **AuthenticationProvider personalizado**.

Esses componentes possuem responsabilidades diferentes e trabalham em conjunto.

---

## Filtro personalizado

O filtro personalizado (`CustomFilter`) participa diretamente do processamento da requisição HTTP.

Sua responsabilidade está relacionada à identificação da informação necessária para o mecanismo de autenticação personalizada.

Por estar inserido na `SecurityFilterChain`, ele pode analisar a requisição antes que ela continue seu processamento normal.

No projeto, o filtro é adicionado utilizando:

```java
.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
```

Isso determina sua posição na cadeia de filtros.

O filtro, portanto, representa o ponto de entrada da autenticação personalizada dentro do processamento da requisição.

---

## AuthenticationProvider personalizado

O `AuthenticationProvider` representa o componente responsável por realizar a lógica de autenticação de um determinado tipo de credencial.

No projeto foi criado o:

`SenhaMasterAuthenticationProvider`

Ele implementa uma lógica específica para autenticar utilizando a credencial definida pelo mecanismo de senha master.

A responsabilidade desse componente é diferente da responsabilidade do filtro.

Enquanto o filtro está relacionado ao **processamento da requisição e identificação da credencial**, o provider está relacionado à **validação da autenticação**.

---

## supports()

Um `AuthenticationProvider` possui o método `supports()`.

Ele permite indicar quais tipos de objeto de autenticação aquele provider sabe processar.

Isso é importante porque uma aplicação pode possuir diferentes mecanismos de autenticação e, consequentemente, diferentes providers.

O provider deve reconhecer apenas os tipos de autenticação para os quais foi desenvolvido.

---

## authenticate()

O método `authenticate()` representa o ponto em que o provider realiza o processamento da autenticação.

É nesse momento que a lógica específica do mecanismo implementado pelo provider é executada.

No caso do `SenhaMasterAuthenticationProvider`, essa lógica está relacionada à credencial utilizada pela autenticação personalizada.

---

# 6. Integração entre filtro e AuthenticationProvider

O filtro e o provider não possuem exatamente a mesma responsabilidade.

Eles fazem parte de etapas diferentes do mesmo mecanismo.

Podemos pensar conceitualmente da seguinte forma:

```text
Requisição HTTP
       ↓
CustomFilter
       ↓
Identificação da credencial
       ↓
Autenticação personalizada
       ↓
SenhaMasterAuthenticationProvider
       ↓
Validação da credencial
       ↓
Autenticação estabelecida
```

O filtro está mais próximo da requisição HTTP.

O provider está mais próximo da lógica de autenticação.

Essa separação evita concentrar toda a lógica em um único componente.

---

## Registro do provider

Para que o provider personalizado participe do mecanismo de segurança, ele é registrado na configuração da `SecurityFilterChain`:

```java
.authenticationProvider(senhaMasterAuthenticationProvider)
```

Assim, o Spring Security passa a considerar esse provider durante o processamento das autenticações compatíveis com ele.

---

## Registro do filtro

Da mesma forma, o filtro personalizado é inserido na cadeia:

```java
.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class)
```

Com isso, os dois componentes passam a fazer parte da estrutura de segurança da aplicação.

Podemos resumir:

**SecurityFilterChain**

→ organiza o fluxo de segurança.

**CustomFilter**

→ participa do processamento da requisição e da obtenção da informação necessária para a autenticação personalizada.

**SenhaMasterAuthenticationProvider**

→ processa a lógica específica de autenticação.

---

# 7. Fluxo de autenticação

Os diferentes mecanismos estudados no projeto podem ser entendidos a partir de seus fluxos.

## Fluxo tradicional

```text
Requisição
   ↓
Usuário e senha
   ↓
Localização do usuário
   ↓
UserDetailsService
   ↓
Verificação da senha
   ↓
PasswordEncoder
   ↓
Autenticação
```

Nesse fluxo, `UserDetailsService` e `PasswordEncoder` possuem responsabilidades distintas.

---

## Fluxo personalizado

```text
Requisição
   ↓
CustomFilter
   ↓
Identificação da credencial
   ↓
SenhaMasterAuthenticationProvider
   ↓
Validação
   ↓
Autenticação
```

Aqui, o mecanismo foi personalizado para atender uma necessidade diferente da autenticação tradicional.

A principal ideia é perceber que o Spring Security permite adaptar o processo de autenticação através da combinação de filtros e providers.

---

# 8. Conceitos importantes

## Separação de responsabilidades

Um dos principais conceitos observados no projeto é a divisão das responsabilidades entre os componentes.

Cada elemento participa de uma parte específica do processo.

Por exemplo:

* `UserDetailsService` → fornece informações do usuário;
* `PasswordEncoder` → trabalha com a validação da senha;
* `CustomFilter` → participa do processamento da requisição;
* `AuthenticationProvider` → executa a lógica específica de autenticação;
* `SecurityFilterChain` → organiza os componentes dentro do fluxo de segurança.

---

## Ordem dos filtros

A ordem dos filtros não é apenas uma questão de organização.

Ela influencia diretamente o comportamento do processo de segurança.

No projeto, o filtro personalizado foi colocado antes do `UsernamePasswordAuthenticationFilter`, permitindo que o mecanismo personalizado seja processado na posição definida dentro da cadeia.

---

## Diferentes mecanismos de autenticação

O Spring Security não está limitado à autenticação tradicional com usuário e senha.

É possível implementar diferentes mecanismos de autenticação, desde que eles sejam integrados corretamente ao fluxo de segurança.

O projeto demonstra isso através da coexistência de:

* autenticação tradicional;
* autenticação personalizada.

---

## AuthenticationProvider como mecanismo extensível

O `AuthenticationProvider` permite implementar regras específicas para diferentes tipos de autenticação.

Isso torna o mecanismo de autenticação extensível, permitindo que a aplicação possua providers especializados para diferentes necessidades.

No projeto, essa ideia é representada pelo `SenhaMasterAuthenticationProvider`.

---

## Filtros como parte do processamento da requisição

Os filtros do Spring Security trabalham diretamente no fluxo das requisições.

Um filtro personalizado pode ser utilizado quando é necessário analisar alguma informação da requisição antes que ela continue seu processamento.

Por isso, compreender a `SecurityFilterChain` e a ordem dos filtros é fundamental para compreender autenticações personalizadas.

---

## Autenticação não é autorização

Outro ponto fundamental é não confundir os dois conceitos.

A autenticação estabelece uma identidade.

A autorização utiliza essa identidade para determinar quais operações ou recursos podem ser acessados.

Portanto, implementar um mecanismo de autenticação não significa, por si só, definir todas as regras de autorização da aplicação.

---

## Visão geral do projeto

Os principais conceitos estudados podem ser relacionados da seguinte maneira:

```text
                    Spring Security
                          │
                          ▼
                 SecurityFilterChain
                          │
             ┌────────────┴────────────┐
             │                         │
             ▼                         ▼
   Autenticação tradicional    Autenticação personalizada
             │                         │
             │                         ▼
             │                  CustomFilter
             │                         │
             ▼                         ▼
    UserDetailsService       AuthenticationProvider
             │                         │
             ▼                         ▼
      PasswordEncoder        SenhaMasterAuthenticationProvider
             │                         │
             └────────────┬────────────┘
                          ▼
                    Autenticação
                          │
                          ▼
                     Autorização
```

Essa estrutura representa os conceitos principais trabalhados no projeto e mostra como os diferentes componentes se relacionam dentro do Spring Security.
