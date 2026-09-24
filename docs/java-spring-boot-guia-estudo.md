# Java + Spring Boot na prática

> Guia de revisão baseado no projeto desenvolvido em Java 25, Spring Boot, Gradle, JPA, SQLite, Spring Security, JWT, CORS, Rate Limiting, testes com JUnit/Mockito e Docker.

---

## 0. Como usar este guia com o código

Este documento descreve a implementação que está neste repositório. Ao encontrar um termo novo, abra o arquivo indicado e acompanhe o fluxo no código. Não é necessário entender tudo na primeira leitura: comece pelo caminho de uma requisição e volte às seções específicas quando surgir uma dúvida.

### 0.1 Mapa de leitura recomendado

```text
1. MainApp                         -> inicializa o Spring Boot
2. CustomerController              -> recebe uma requisição HTTP
3. CustomerService                 -> aplica a regra de negócio
4. CustomerValidator               -> valida os dados
5. CustomerRepository + Customer   -> grava/lê no SQLite
6. SecurityConfig + filtros        -> protege as rotas com JWT
7. testes em src/test/java          -> mostram como cada regra é verificada
```

### 0.2 Estrutura real do projeto

```text
src/main/java/com/example/demo/
├── MainApp.java                    # ponto de entrada
├── customers/                      # funcionalidade de clientes
├── security/                       # login, JWT, usuários e filtros
└── exceptions/                     # erros convertidos em respostas HTTP

src/main/resources/
└── application.properties          # configuração da aplicação

src/test/java/com/example/demo/     # testes automatizados
```

### 0.3 Convenções usadas no código

- nomes de classes começam com letra maiúscula: `CustomerService`;
- métodos e variáveis usam `camelCase`: `findById`, `passwordEncoder`;
- `final` em dependências indica que a referência não será trocada;
- pacotes são minúsculos e separados por ponto: `customers.service`;
- `Request` representa dados recebidos da API; `Response`, dados devolvidos pela API.

---

## 1. Ambiente de desenvolvimento

### 1.1 JDK
O JDK é o kit de desenvolvimento Java. Ele inclui compilador, ferramentas de desenvolvimento e a JVM necessária para executar aplicações Java.

No projeto foi utilizado Java 25.

Comandos úteis:

```bash
java -version
javac -version
```

### 1.2 IntelliJ IDEA
O IntelliJ é a IDE usada no projeto.

Pontos importantes:
- abrir sempre a pasta raiz do projeto;
- o IntelliJ reconhece projetos Gradle;
- `src/main/java` contém o código principal;
- `src/test/java` contém os testes;
- `build/` contém arquivos gerados pelo Gradle;
- `Run → Edit Configurations` permite configurar execução, variáveis de ambiente e working directory.

### 1.3 Abrindo um projeto movido de pasta
Use:

```text
File → Open
```

Selecione a pasta que contém:

```text
build.gradle
settings.gradle
gradlew
src/
```

### 1.4 Variáveis de ambiente no IntelliJ
Na Run Configuration, é possível definir variáveis como:

```text
JWT_SECRET=...
SERVER_PORT=5001
```

Também é possível usar um arquivo `.env`, desde que a configuração aponte para o arquivo correto.

No projeto, `JWT_SECRET` é obrigatório. Para carregar `.env` no IntelliJ, crie uma configuração **Spring Boot** para `com.example.demo.MainApp`, use o plugin EnvFile (ou copie as variáveis ao campo **Environment variables**) e aponte para `$PROJECT_DIR$/.env`. O arquivo `.env.example` mostra os nomes das variáveis sem expor valores reais.

---

## 2. Gradle

### 2.1 O que é
Gradle é a ferramenta de build e gerenciamento de dependências.

Comparação aproximada com .NET:

```text
Gradle
≈ MSBuild + NuGet + dotnet CLI
```

### 2.2 `build.gradle`
É o arquivo principal de configuração do projeto.

Equivale conceitualmente ao `.csproj`.

Exemplo:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

No `build.gradle` deste projeto, os grupos de dependências têm estes papéis:

- `spring-boot-starter-web`: controllers REST e servidor HTTP embutido;
- `spring-boot-starter-data-jpa`: JPA, Hibernate e repositórios;
- `spring-boot-starter-security`: autenticação e autorização;
- `jjwt-*`: criação e validação do JWT;
- `bucket4j-core`: limite de tentativas de login;
- `sqlite-jdbc`: driver que conversa com o arquivo SQLite;
- `spring-boot-starter-test`: JUnit, Mockito e ferramentas de teste.

### 2.3 `settings.gradle`
Define o nome do projeto e pode declarar projetos adicionais.

### 2.4 Gradle Wrapper
Arquivos:

```text
gradlew
gradlew.bat
gradle/
```

Permitem executar Gradle sem depender de instalação global.

Exemplos:

```bash
./gradlew build
./gradlew test
./gradlew bootJar
```

### 2.5 Reload do Gradle
Quando uma dependência é adicionada, pode ser necessário:

```text
Gradle → Reload All Gradle Projects
```

ou:

```bash
./gradlew build --refresh-dependencies
```

### 2.6 JAR
O Spring Boot gera um JAR executável contendo a aplicação e suas dependências.

Normalmente em:

```text
build/libs/
```

O fat JAR pode ser executado com:

```bash
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

Antes de executar o JAR, defina `JWT_SECRET`, pois a aplicação não inicia sem essa variável.

---

## 3. Packages e organização do projeto

### 3.1 Package x pasta
Em Java, packages organizam classes e também determinam namespace.

Exemplo:

```java
package com.example.demo.customers.service;
```

Normalmente corresponde a:

```text
src/main/java/com/example/demo/customers/service
```

### 3.2 Package raiz
A classe principal ficou em:

```text
com.example.demo.MainApp
```

Como ela está acima dos demais packages, o Spring consegue fazer component scan.

### 3.3 Organização por feature
Estrutura utilizada:

```text
com.example.demo
├── customers
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   ├── service
│   └── validator
├── security
│   ├── config
│   ├── controller
│   ├── dto
│   ├── filter
│   ├── model
│   ├── repository
│   └── service
├── exceptions
└── MainApp
```

Essa organização facilita navegar por funcionalidade.

Para seguir o código de clientes, abra os arquivos nesta ordem:

```text
CustomerController
→ CustomerService
→ CustomerValidator
→ CustomerRepository
→ Customer
```

Em `security`, a ordem mais útil para iniciantes é `AuthController`, `UserService`, `TokenService`, `JwtAuthenticationFilter` e, por fim, `SecurityConfig`.

### 3.4 Movendo classes
No IntelliJ, prefira:

```text
Refactor → Move
```

Assim o package e imports são ajustados automaticamente.

---

## 4. Fundamentos do Spring Boot

### 4.1 `@SpringBootApplication`
Marca a classe principal.

Exemplo:

```java
@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
```

Ela combina várias funcionalidades do Spring, inclusive configuração automática e component scanning.

Na implementação, essa classe é `src/main/java/com/example/demo/MainApp.java`. Ao executar `MainApp.main`, a chamada `SpringApplication.run(...)` cria o container Spring, encontra as classes anotadas abaixo do package `com.example.demo` e inicia o servidor HTTP.

### 4.2 Embedded Tomcat
A aplicação web não precisa de um Tomcat externo.

O Spring Boot sobe um servidor embutido.

### 4.3 `application.properties`
Arquivo principal de configuração:

```text
src/main/resources/application.properties
```

Exemplo:

```properties
server.port=${SERVER_PORT:5000}

spring.datasource.url=${DB_URL:jdbc:sqlite:customers.db}
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=${JWT_SECRET}
```

O arquivo real também possui `spring.application.name=demo` e as propriedades opcionais `app.admin.username` e `app.admin.password`. Elas são usadas por `AdminSeed` para criar o primeiro administrador somente quando ambas estiverem preenchidas.

### 4.4 Valor padrão
A sintaxe:

```properties
server.port=${SERVER_PORT:5000}
```

significa:

- se `SERVER_PORT` existir, use seu valor;
- caso contrário, use `5000`.

---

## 5. IoC e Dependency Injection

### 5.1 Inversion of Control
O Spring cria e gerencia objetos para você.

Em vez de:

```java
CustomerRepository repository = new CustomerRepository();
```

o Spring injeta a dependência.

### 5.2 Bean
Bean é um objeto gerenciado pelo container do Spring.

### 5.3 Principais anotações

#### `@Component`
Classe genérica gerenciada pelo Spring.

```java
@Component
public class CustomerValidator {
}
```

#### `@Service`
Classe de serviço/regra de negócio.

```java
@Service
public class CustomerService {
}
```

#### `@Repository`
Camada de acesso a dados.

```java
public interface UserRepository extends JpaRepository<User, UUID> {
}
```

Neste projeto, `CustomerRepository` e `UserRepository` não declaram `@Repository` explicitamente. Isso funciona porque o Spring Data identifica interfaces que estendem `JpaRepository` e cria a implementação automaticamente.

#### `@RestController`
Controller HTTP.

```java
@RestController
@RequestMapping("/customers")
public class CustomerController {
}
```

#### `@Configuration`
Classe de configuração.

```java
@Configuration
public class SecurityConfig {
}
```

#### `@Bean`
Registra manualmente um objeto no container.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 5.4 Injeção por construtor
Preferida no projeto:

```java
public CustomerService(
        CustomerRepository repository,
        CustomerValidator validator) {

    this.repository = repository;
    this.validator = validator;
}
```

### 5.5 Escopo padrão
Beans Spring são singleton por padrão.

Isso não significa que guardam estado de usuário.

Services normalmente devem ser stateless.

Comparação conceitual:

```text
Spring Bean singleton
≈ AddSingleton no .NET
```

Mas o padrão arquitetural do Spring usa services singleton e stateless com muita frequência.

---

## 6. API REST

### 6.1 Controller
Responsável por HTTP.

Exemplo:

```java
@GetMapping("/{id}")
public CustomerResponse findById(@PathVariable UUID id) {
    return service.findById(id);
}
```

Esse método existe em `CustomerController`. `@PathVariable UUID id` converte a parte `{id}` da URL para um UUID Java. Se a URL for `/customers/abc`, a conversão falha antes de chegar ao service porque `abc` não é um UUID válido.

### 6.2 Service
Responsável por regras de negócio.

```java
public CustomerResponse findById(UUID id) {
    Customer customer = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

    return new CustomerResponse(
            customer.getId(),
            customer.getName()
    );
}
```

### 6.3 Repository
Responsável por persistência.

```java
public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {
}
```

### 6.4 Regra adotada

```text
Controller
→ HTTP

Service
→ regra de negócio

Repository
→ banco de dados
```

#### Rotas realmente expostas

| Método | Rota | Classe/método | Resultado principal |
| --- | --- | --- | --- |
| GET | `/customers` | `CustomerController.findAll` | lista clientes |
| GET | `/customers/{id}` | `CustomerController.findById` | retorna um cliente ou 404 |
| POST | `/customers` | `CustomerController.create` | cria e retorna 201 |
| PUT | `/customers/{id}` | `CustomerController.update` | altera e retorna 200 |
| DELETE | `/customers/{id}` | `CustomerController.delete` | exclui e retorna 200 |
| POST | `/auth/register` | `AuthController.register` | cria usuário comum e retorna 201 |
| POST | `/auth/login` | `AuthController.login` | retorna o JWT |

As rotas de clientes exigem JWT. As duas rotas sob `/auth` são públicas; isso é configurado em `SecurityConfig`.

---

## 7. DTOs

### 7.1 Por que usar
Evita expor diretamente entidades JPA.

Também separa:
- modelo de persistência;
- modelo de entrada;
- modelo de saída.

### 7.2 Records
Foram usados `record`s:

```java
public record CustomerResponse(
        UUID id,
        String name,
        String email
) {
}
```

### 7.3 Tipos criados
Exemplos:

```text
CreateCustomerRequest
UpdateCustomerRequest
DeleteCustomerRequest
CustomerResponse

LoginRequest
LoginResponse
RegisterRequest
ChangePasswordRequest
UserResponse
```

### 7.4 Mapeamento manual
Foi mantido manualmente para aprendizado.

Exemplo:

```java
return new CustomerResponse(
        customer.getId(),
        customer.getName(),
        customer.getEmail()
);
```

Mais tarde pode ser utilizado MapStruct ou outra biblioteca.

No projeto, quem faz esse mapeamento é principalmente `CustomerService` e `UserService`. Por exemplo, uma entidade `User` possui o campo `password`, mas `UserResponse` não o possui. Assim, uma resposta HTTP nunca deve devolver o hash da senha ao cliente.

---

## 8. Conceitos Java usados no projeto

### 8.1 `final`
Em campos:

```java
private final UserRepository repository;
```

Significa que a referência não pode ser reatribuída após inicialização.

Comparação aproximada:

```text
final
≈ readonly em C#
```

### 8.2 Comparação de objetos
Não usar:

```java
string1 == string2
```

Para conteúdo:

```java
string1.equals(string2)
```

Para UUID:

```java
id1.equals(id2)
```

### 8.3 UUID
Equivalente conceitual ao `Guid` do .NET.

```java
UUID id = UUID.randomUUID();
```

### 8.4 Optional
Repository pode retornar:

```java
Optional<User>
```

Uso comum:

```java
User user = repository.findByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found"));
```

`Optional` não substitui o tratamento de erro; ele torna explícito que a consulta pode não encontrar nada. No projeto, `orElseThrow(...)` converte esse caso em `NotFoundException`, que depois vira HTTP 404 pelo `GlobalExceptionHandler`.

### 8.5 Streams

```java
return repository.findAll()
        .stream()
        .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.isActive(),
                user.getRole()
        ))
        .toList();
```

---

## 9. JPA e Hibernate

### 9.1 JPA
JPA é a especificação de persistência.

Hibernate é uma implementação.

Comparação:

```text
JPA/Hibernate
≈ Entity Framework Core
```

### 9.2 Entidade

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;
}
```

### 9.3 Repository

```java
public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {
}
```

O Spring gera automaticamente operações de CRUD.

### 9.4 Query Methods
O Spring Data interpreta o nome dos métodos.

Exemplos:

```java
Optional<User> findByUsername(String username);

boolean existsByRole(Role role);

int countByRole(Role role);
```

Não é necessário escrever SQL para esses casos.

Na implementação, os métodos de `UserRepository` são usados por `UserService` e `AdminSeed`:

- `findByUsername` localiza o usuário no login e no `CurrentUserService`;
- `existsByRole(Role.ADMIN)` evita criar mais de um admin inicial;
- `countByRole(Role.ADMIN)` impede excluir o último administrador.

---

## 10. SQLite

### 10.1 Configuração

```properties
spring.datasource.url=${DB_URL:jdbc:sqlite:customers.db}
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
```

### 10.2 `ddl-auto`

#### `update`
Cria e ajusta estrutura automaticamente.

```properties
spring.jpa.hibernate.ddl-auto=update
```

Bom para desenvolvimento.

#### `validate`
Só valida se o banco está compatível.

#### `none`
Não faz alterações.

### 10.3 Produção
Em sistemas profissionais, migrations normalmente são feitas com:
- Flyway;
- Liquibase.

---

## 11. Validação

### 11.1 Validator próprio

```java
@Component
public class CustomerValidator {
}
```

Foi usado para centralizar regras de validação do cliente.

`CustomerValidator.validate(Customer)` verifica atualmente duas regras: `name` e `email` não podem ser nulos nem vazios. `CustomerService.add` e `CustomerService.update` chamam o validator antes de salvar no repositório.

### 11.2 Tipos de validação
É útil distinguir:

```text
validação técnica
→ campo obrigatório, e-mail válido, tamanho

regra de negócio
→ usuário pode ou não fazer determinada operação
```

### 11.3 Bean Validation
Pode ser aprofundado com:

```java
@NotNull
@NotBlank
@Email
@Size
```

---

## 12. Tratamento global de exceções

### 12.1 Exceções customizadas
Foram criadas:

```text
NotFoundException
BadRequestException
InternalServerErrorException
```

### 12.2 `@RestControllerAdvice`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Permite tratar exceções globalmente e convertê-las em respostas HTTP.

Exemplo conceitual:

```text
NotFoundException
→ HTTP 404

BadRequestException
→ HTTP 400
```

No arquivo `GlobalExceptionHandler.java`, as três conversões atuais são:

| Exceção | Status HTTP | Quando aparece |
| --- | --- | --- |
| `NotFoundException` | 404 | cliente ou usuário não encontrado |
| `BadRequestException` | 400 | dados inválidos ou acesso negado pela regra de negócio |
| `InternalServerErrorException` | 500 | erro interno explicitamente convertido pela aplicação |

O corpo retornado hoje é apenas uma `String` com a mensagem. Em uma API de produção, uma melhoria comum é retornar um JSON padronizado com data, status, mensagem e caminho da requisição.

---

## 13. Spring Security

### 13.1 Security Filter Chain
O Spring Security trabalha com uma cadeia de filtros.

Fluxo simplificado:

```text
HTTP Request
↓
Security Filters
↓
Controller
```

### 13.2 Configuração

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        .cors(cors -> {})
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### 13.3 Stateless
A API não depende de sessão HTTP.

```java
SessionCreationPolicy.STATELESS
```

A autenticação é enviada a cada requisição via JWT.

Na classe `SecurityConfig`, os dois filtros próprios são inseridos antes de `UsernamePasswordAuthenticationFilter`:

1. `RateLimitFilter` conta tentativas de `/auth/login`;
2. `JwtAuthenticationFilter` lê o cabeçalho `Authorization` e preenche o usuário autenticado no `SecurityContext`.

Isso explica por que um controller pode receber a requisição já autenticada sem precisar ler ou validar o token diretamente.

---

## 14. Usuários e roles

### 14.1 Role

```java
public enum Role {
    USER,
    ADMIN
}
```

### 14.2 Persistência

```java
@Enumerated(EnumType.STRING)
private Role role;
```

No banco fica:

```text
USER
ADMIN
```

### 14.3 Regras implementadas
Exemplos:

```text
/auth/**
→ público

/customers/**
→ qualquer usuário autenticado

/users/**
→ autenticado, com regras adicionais
```

USER:
- acessa seus próprios dados;
- altera sua própria senha.

ADMIN:
- acessa outros usuários;
- cria administrador;
- altera senha de outros usuários;
- não pode deixar o sistema sem administrador.

As rotas de usuários existentes são:

| Método | Rota | Regra no código |
| --- | --- | --- |
| GET | `/users/get-by-id/{id}` | usuário comum só vê o próprio registro; admin pode ver qualquer um |
| POST | `/users/change-password` | usuário comum precisa informar a senha antiga e só altera a própria conta |
| GET | `/users/get-all` | intenção: apenas admin |
| POST | `/users/create-admin` | intenção: apenas admin |
| DELETE | `/users/delete/{id}` | intenção: apenas admin; último admin não pode ser removido |

#### Autorização por método ativa

`UserController` usa `@PreAuthorize("hasRole('ADMIN')")` em `get-all`, `create-admin` e `delete`. A anotação `@EnableMethodSecurity`, presente em `SecurityConfig`, ativa a interpretação dessas regras pelo Spring Security.

O filtro JWT cria autoridades no formato `ROLE_` + role, por exemplo `ROLE_ADMIN`. Por isso, a expressão `hasRole('ADMIN')` funciona: o Spring acrescenta e procura o prefixo `ROLE_` internamente. Um usuário autenticado com `ROLE_USER` recebe 403 ao tentar executar esses métodos; um admin pode continuar.

Essa proteção deve ser mantida também com testes de integração, pois testes unitários de `UserService` não iniciam o Spring nem executam a anotação `@PreAuthorize`.

---

## 15. PasswordEncoder e BCrypt

### 15.1 Bean

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 15.2 Gravando senha

```java
user.setPassword(
        passwordEncoder.encode(password)
);
```

### 15.3 Validando senha

```java
if (!passwordEncoder.matches(
        request.password(),
        user.getPassword())) {

    throw new BadRequestException("Invalid password");
}
```

Nunca armazenar senha em texto puro.

---

## 16. JWT

### 16.1 Conceito
JWT é um token assinado enviado pelo cliente.

Fluxo:

```text
login
↓
usuário/senha válidos
↓
JWT gerado
↓
cliente armazena token
↓
envia Bearer Token nas próximas requisições
```

### 16.2 TokenService
Responsabilidades:
- gerar token;
- extrair username;
- extrair role;
- validar token;
- validar expiração.

### 16.3 Claims
Exemplo:

```java
.claim("role", role.name())
```

### 16.4 Secret
Não deve ficar hardcoded.

```properties
jwt.secret=${JWT_SECRET}
```

`TokenService` recebe esse valor no construtor com `@Value("${jwt.secret}")` e o transforma em uma `SecretKey` HMAC. A chave não é enviada ao cliente; ela é usada apenas para assinar e verificar tokens.

---

## 17. JwtAuthenticationFilter

Foi implementado com:

```java
extends OncePerRequestFilter
```

Fluxo:

```text
Authorization header
↓
Bearer token
↓
TokenService valida
↓
extrai username
↓
extrai role
↓
cria Authentication
↓
SecurityContextHolder
```

Exemplo:

```java
UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(
                    new SimpleGrantedAuthority(
                        "ROLE_" + role
                    )
                )
        );

SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
```

No código real, token ausente ou inválido não gera resposta diretamente no filtro: ele apenas continua a cadeia sem autenticar o usuário. Como as demais rotas exigem autenticação, o Spring Security responde 401 depois.

---

## 18. Autorização

### 18.1 Autenticação x autorização

```text
Autenticação
→ quem é você?

Autorização
→ o que você pode fazer?
```

### 18.2 Usuário logado
Como o filter colocou o username no principal:

```java
Authentication authentication =
        SecurityContextHolder
                .getContext()
                .getAuthentication();

String username = authentication.getName();
```

### 18.3 Regra de acesso
O ponto importante é verificar a role do usuário logado, não a role do usuário alvo.

Exemplo:

```java
if (loggedUser.getRole() == Role.USER) {
    if (!userId.equals(loggedUser.getId())) {
        throw new BadRequestException("Access denied");
    }
}
```

---

## 19. CurrentUserService

Havia repetição para recuperar o usuário autenticado.

Foi extraído para uma classe dedicada:

```java
@Service
public class CurrentUserService {

    private final UserRepository repository;

    public CurrentUserService(UserRepository repository) {
        this.repository = repository;
    }

    public User get() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        return repository.findByUsername(username)
                .orElseThrow(() ->
                    new NotFoundException(
                        "Logged user not found"
                    )
                );
    }
}
```

Uso:

```java
User loggedUser = currentUserService.get();
```

Benefícios:
- evita duplicação;
- centraliza lógica;
- facilita manutenção;
- pode ser reutilizado por várias services.

No fluxo real, `UserService.changePassword` usa `currentUserService.get()` para descobrir quem fez a chamada e comparar essa pessoa com o usuário alvo. Isso impede que um usuário comum altere a senha de outra conta.

---

## 20. AdminSeed

Foi utilizada uma classe para criar o primeiro administrador.

Exemplo conceitual:

```java
@Component
public class AdminSeed implements CommandLineRunner {

    @Override
    public void run(String... args) {

        if (!repository.existsByRole(Role.ADMIN)) {
            // cria admin inicial
        }
    }
}
```

Na versão atual, o admin não usa credenciais fixas no código. `AdminSeed` lê `ADMIN_USERNAME` e `ADMIN_PASSWORD` por meio das propriedades `app.admin.username` e `app.admin.password`. Se uma das duas estiver vazia, o método termina sem criar usuário. Se ambas existirem e ainda não houver um admin, a senha é armazenada com BCrypt.

### Quem chama?
Ninguém chama manualmente.

O Spring:
1. encontra a classe pelo `@Component`;
2. cria o bean;
3. injeta dependências;
4. detecta `CommandLineRunner`;
5. executa `run()` na inicialização.

---

## 21. CORS

### 21.1 Conceito
CORS controla quais origens do navegador podem chamar a API.

Não é autenticação.

### 21.2 Configuração

```java
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration =
            new CorsConfiguration();

    configuration.setAllowedOrigins(
            List.of("http://localhost:3000")
    );

    configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
            )
    );

    configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
    );

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration(
            "/**",
            configuration
    );

    return source;
}
```

Import:

```java
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
```

Importante:

```text
allowedOrigins
→ origem do frontend

server.port
→ porta da API
```

---

## 22. Rate Limiting

### 22.1 Rate limit de infraestrutura
Normalmente fica em:
- Nginx;
- Cloudflare;
- API Gateway;
- Load Balancer.

Objetivo:
- conter flood;
- proteger infraestrutura;
- limitar abuso genérico.

### 22.2 Rate limit de negócio
Faz sentido na aplicação.

Exemplos:
- 5 tentativas de login;
- 3 pedidos de recuperação de senha;
- limite de MFA;
- limite por usuário.

### 22.3 Bucket4j
Dependência:

```groovy
implementation 'com.bucket4j:bucket4j-core:8.10.1'
```

Import:

```java
import io.github.bucket4j.Bucket;
```

### 22.4 Exemplo do filtro

```java
@Component
public class RateLimitFilter
        extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI()
                .equals("/auth/login")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String ip = request.getRemoteAddr();

        Bucket bucket =
                buckets.computeIfAbsent(
                        ip,
                        key -> createBucket()
                );

        if (bucket.tryConsume(1)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        response.setStatus(429);
        response.getWriter()
                .write("Too many requests");
    }
}
```

O `createBucket()` da implementação define capacidade de **5 tentativas** e recarrega **5 tokens a cada minuto**, por endereço IP. A sexta tentativa dentro desse período recebe HTTP 429 e a mensagem `Too many requests`.

### 22.5 Limitação em memória
Com `ConcurrentHashMap`:
- cada instância tem seu próprio contador;
- reiniciar a aplicação perde os dados;
- múltiplas instâncias não compartilham limite.

### 22.6 Redis
Em produção, pode ser utilizado Redis:

```text
API 1 ─┐
API 2 ─┼── Redis
API 3 ─┘
```

Exemplo conceitual:

```text
login-attempts:usuario123
valor = 3
TTL = 10 minutos
```

Fluxo:

```text
incrementa contador
↓
se primeira tentativa, define TTL
↓
se abaixo do limite, libera
↓
se acima, bloqueia
```

Operações devem ser atômicas.

---

## 23. Secrets e configuração

### 23.1 `.env`
O `.env` pode conter:

```env
JWT_SECRET=...
SERVER_PORT=5000
ADMIN_USERNAME=admin
ADMIN_PASSWORD=...
```

`DB_URL` é opcional: sem ela, o arquivo local `customers.db` é usado. Em Docker, o Compose define `DB_URL=jdbc:sqlite:/data/customers.db` para que o banco fique dentro do volume.

Nunca deve ser versionado.

### 23.2 `.gitignore`

```gitignore
.env
*.db
.gradle/
build/
.idea/
*.iml
```

### 23.3 Spring e `.env`
Spring Boot não lê automaticamente um `.env` genérico.

É necessário:
- configurar as variáveis no ambiente;
- configurar o IntelliJ para carregar o `.env`;
- ou usar Docker Compose.

### 23.4 Placeholder

```properties
jwt.secret=${JWT_SECRET}
```

Se a variável não existir, ocorre erro semelhante a:

```text
Could not resolve placeholder 'JWT_SECRET'
```

---

## 24. Git

### 24.1 Status

```bash
git status
```

### 24.2 Arquivos gerados
Normalmente não versionar:

```text
.gradle/
build/
.idea/
*.iml
*.db
.env
```

### 24.3 Código fonte
Versionar:

```text
src/
build.gradle
settings.gradle
gradlew
gradle/
Dockerfile
docker-compose.yaml
docs/
README.md
demo-api.postman_collection.json
```

---

## 25. Testes unitários

### 25.1 Objetivo
Teste unitário testa uma unidade isolada de código.

No projeto, normalmente:

```text
Service
```

com dependências simuladas.

### 25.2 JUnit 5
Exemplo:

```java
@Test
void shouldReturnCustomerById() {
}
```

### 25.3 Assertions

```java
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertThrows(
    NotFoundException.class,
    () -> service.findById(id)
);
```

### 25.4 Mockito
Serve para criar mocks.

```java
@Mock
private CustomerRepository repository;
```

### 25.5 Inicialização

```java
@BeforeEach
void setup() {
    MockitoAnnotations.openMocks(this);
}
```

Ou:

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
}
```

### 25.6 `when().thenReturn()`

```java
when(repository.findById(id))
        .thenReturn(
            Optional.of(customer)
        );
```

Significa:

```text
quando esse método for chamado
→ retorne este valor falso/controlado
```

### 25.7 `verify()`

```java
verify(repository).findById(id);
```

Confirma que a dependência foi chamada.

### 25.8 Exemplo

```java
@Test
void shouldReturnCustomerById() {

    UUID id = UUID.randomUUID();

    Customer customer = new Customer();
    customer.setId(id);
    customer.setName("Luiz");

    when(repository.findById(id))
            .thenReturn(
                Optional.of(customer)
            );

    CustomerResponse result =
            service.findById(id);

    assertEquals(id, result.id());
    assertEquals("Luiz", result.name());

    verify(repository).findById(id);
}
```

### 25.9 Testando exceção

```java
@Test
void shouldThrowWhenCustomerDoesNotExist() {

    UUID id = UUID.randomUUID();

    when(repository.findById(id))
            .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.findById(id)
    );
}
```

### 25.10 O que mockar
Exemplos do projeto:

```text
CustomerRepository
CustomerValidator
UserRepository
PasswordEncoder
TokenService
CurrentUserService
```

### 25.11 Execução

```bash
./gradlew test
```

No IntelliJ:
- botão direito na classe;
- `Run`;
- ou botão verde ao lado do teste.

---

## 26. Testes unitários implementados no projeto

### 26.1 Onde ficam

Os testes seguem a mesma organização de packages do código principal, dentro de:

```text
src/test/java/com/example/demo/
├── customers
│   ├── service/CustomerServiceTest.java
│   └── validator/CustomerValidatorTest.java
└── security
    └── service
        ├── CurrentUserServiceTest.java
        ├── TokenServiceTest.java
        └── UserServiceTest.java
```

Por exemplo, a classe principal:

```text
src/main/java/com/example/demo/customers/service/CustomerService.java
```

é testada por:

```text
src/test/java/com/example/demo/customers/service/CustomerServiceTest.java
```

### 26.2 O que foi testado

Os testes foram concentrados em regras de negócio e segurança, evitando testes de getters, setters ou simples anotações.

| Classe testada | Cenários principais |
| --- | --- |
| `CustomerValidator` | nome e e-mail obrigatórios; cliente válido |
| `CustomerService` | criar, buscar, não encontrar, atualizar, excluir e listar clientes |
| `UserService` | criação com hash, login, permissões, troca de senha e proteção do último admin |
| `CurrentUserService` | identificação do usuário presente no `SecurityContext` |
| `TokenService` | geração, leitura e validação de JWT |

`CustomerService` e `UserService` não acessam banco nos testes: seus repositórios e dependências são mocks Mockito. `TokenService` é testado diretamente porque sua regra é local e não tem dependência externa.

### 26.3 Estrutura AAA

Os testes seguem o padrão **Arrange, Act, Assert**:

```java
@Test
void rejectsLookupOfMissingCustomer() {
    // Arrange: prepara cenário e comportamento do mock
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    // Act + Assert: executa e verifica o resultado esperado
    assertThrows(NotFoundException.class, () -> service.findById(id));
}
```

O padrão torna explícito o que o teste está preparando, qual método está sendo exercitado e qual é o resultado esperado.

### 26.4 Mockito usado no projeto

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerValidator validator;

    @InjectMocks
    private CustomerService service;
}
```

- `@Mock` cria um objeto falso e controlável;
- `@InjectMocks` instancia a classe em teste com seus mocks;
- `when(...).thenReturn(...)` configura a resposta de uma dependência;
- `verify(...)` confirma uma interação importante, como `repository.save(...)`.

### 26.5 Como executar todos os testes

Na raiz do projeto:

```bash
./gradlew test
```

No Windows:

```bat
gradlew.bat test
```

O relatório HTML é gerado em:

```text
build/reports/tests/test/index.html
```

Para executar uma única classe:

```bash
./gradlew test --tests "com.example.demo.security.service.UserServiceTest"
```

Para executar somente um método:

```bash
./gradlew test --tests "com.example.demo.security.service.UserServiceTest.authenticatesActiveUserAndReturnsJwt"
```

### 26.6 Executando no IntelliJ IDEA

Opções mais comuns:

1. abra uma classe `*Test.java` em `src/test/java`;
2. clique no ícone verde ao lado da classe ou do método e escolha **Run**;
3. para a suíte inteira, abra a janela Gradle e execute `Tasks > verification > test`.

O IntelliJ mostra os testes aprovados, falhos e o stack trace de falhas diretamente na janela de execução.

### 26.7 Teste unitário x teste de contexto existente

`DemoApplicationTests` usa `@SpringBootTest` e verifica se o contexto Spring inicia. Esse é um teste de integração/smoke test, não um teste unitário puro. Os testes adicionados na seção anterior não iniciam o contexto Spring e não usam o banco SQLite local.

Ao criar novos testes, prefira testes unitários para regras de negócio. Use `@WebMvcTest` ou `@SpringBootTest` apenas quando for necessário validar integração com HTTP, Spring Security, serialização ou persistência.

---



## 27. Testes de integração

Teste unitário:

```text
classe isolada
+
mocks
```

Teste de integração:

```text
Spring
+
controllers
+
security
+
serialização
+
possível banco
```

### 27.1 Teste de autorização implementado

O arquivo `security/controller/UserAuthorizationIntegrationTest.java` valida a proteção real da rota administrativa `GET /users/get-all`.

Ele sobe o contexto Spring, usa `MockMvc` para simular HTTP e utiliza um banco SQLite em memória exclusivo do teste. `UserService` é substituído por um mock porque o objetivo é testar a camada de segurança, não repetir os testes unitários das regras de usuário.

| Usuário simulado | Resultado esperado | O que comprova |
| --- | --- | --- |
| `ROLE_USER` | HTTP 403 | `@PreAuthorize("hasRole('ADMIN')")` bloqueia o acesso antes de chamar o service |
| `ROLE_ADMIN` | HTTP 200 | a autorização permite a execução normal do controller |

O usuário é anexado à requisição com `SecurityMockMvcRequestPostProcessors.user(...)`. Isso simula as autoridades que, em produção, são criadas pelo `JwtAuthenticationFilter` após validar um token JWT.

### 27.2 `@SpringBootTest`

```java
@SpringBootTest
class ApplicationTests {
}
```

### 27.3 MockMvc
Permite testar endpoints sem abrir uma porta HTTP real.

Pode testar:
- GET;
- POST;
- PUT;
- DELETE;
- status codes;
- JSON;
- security.

### 27.4 Casos importantes
Testar:

```text
200
201
400
401
403
404
429
```

### 27.5 Testcontainers
Mais adiante, pode executar infraestrutura real em containers durante testes.

Exemplo:
- PostgreSQL;
- Redis.

---

## 28. Docker

### 28.1 Dockerfile
Ficou na raiz:

```text
Dockerfile
```

Exemplo multi-stage:

```dockerfile
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon

RUN cp $(find build/libs -name "*.jar" ! -name "*-plain.jar" | head -n 1) app.jar


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 28.2 Multi-stage build

```text
JDK
→ compila

JRE
→ executa
```

A imagem final fica menor.

### 28.3 `.dockerignore`

```text
.gradle
build
.idea
*.iml
*.db
.git
.gitignore
```

### 28.4 Docker Compose

```yaml
services:

  api:
    build: .
    container_name: demo-api

    ports:
      - "5000:5000"

    environment:
      DB_URL: jdbc:sqlite:/data/customers.db
      JWT_SECRET: ${JWT_SECRET:?Defina JWT_SECRET no arquivo .env}
      ADMIN_USERNAME: ${ADMIN_USERNAME:-}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD:-}

    volumes:
      - sqlite-data:/data

    restart: unless-stopped

volumes:
  sqlite-data:
```

O Compose não declara `env_file`. Em vez disso, ele usa a interpolação `${...}`: o Docker Compose lê o arquivo `.env` presente na raiz para preencher `JWT_SECRET`, `ADMIN_USERNAME` e `ADMIN_PASSWORD`. `JWT_SECRET` é obrigatório; os dois valores do admin são opcionais.

### 28.5 Port mapping

```yaml
ports:
  - "5000:5000"
```

Significa:

```text
host:5000
↓
container:5000
```

Importante:

```text
EXPOSE
→ documentação da porta

ports
→ publicação da porta
```

### 28.6 Volume SQLite

```yaml
volumes:
  - sqlite-data:/data
```

Banco:

```text
/data/customers.db
```

Isso evita perder o banco ao recriar o container.

### 28.7 Comandos

Subir:

```bash
docker compose up -d --build
```

Parar:

```bash
docker compose down
```

Remover também o volume e apagar os dados SQLite:

```bash
docker compose down -v
```

Use `-v` somente quando quiser reiniciar os dados locais, pois essa operação remove o banco persistido.

Status:

```bash
docker compose ps
```

Logs:

```bash
docker compose logs -f api
```

---

## 29. Diagnóstico de erros

### 29.1 Stack trace
Ao ler um erro Java, procure principalmente:

```text
Caused by:
```

e continue até o último `Caused by`.

Normalmente é ali que aparece a causa real.

### 29.2 Placeholder não encontrado

```text
Could not resolve placeholder 'JWT_SECRET'
```

Significa que a variável de ambiente não foi encontrada.

### 29.3 Porta ocupada

```text
java.net.BindException:
Endereço já em uso
```

Verifique:

```bash
sudo lsof -i :5000
```

ou:

```bash
ss -ltnp | grep :5000
```

### 29.4 Docker ocupando porta
Pode ocorrer:

```text
IntelliJ
→ tenta usar 5000

Docker
→ já usa 5000

resultado
→ BindException
```

### 29.5 Connection refused
Verifique:
1. aplicação subiu;
2. porta interna;
3. porta publicada;
4. container está rodando.

```bash
docker compose ps
docker compose logs api
```

### 29.6 Dependência Gradle não encontrada
Após adicionar dependência:

```text
Gradle → Reload All Gradle Projects
```

ou:

```bash
./gradlew build --refresh-dependencies
```

---

## 30. Arquitetura final

Fluxo de uma requisição autenticada:

```text
HTTP Request
↓
RateLimitFilter
↓
JwtAuthenticationFilter
↓
Spring Security
↓
Controller
↓
Service
↓
Validator
↓
Repository
↓
JPA / Hibernate
↓
SQLite
```

### 30.1 Exemplo completo: criar um cliente

Use esse caminho para conectar a documentação ao código:

```text
1. Cliente HTTP envia POST /customers com JSON
2. JwtAuthenticationFilter confirma o Bearer token
3. CustomerController recebe CreateCustomerRequest
4. CustomerService cria um Customer e define UUID/data
5. CustomerValidator verifica name e email
6. CustomerRepository.save delega a gravação ao JPA/Hibernate
7. Hibernate grava no SQLite
8. CustomerService converte Customer para CustomerResponse
9. Controller devolve HTTP 201 com JSON
```

Na prática, comece lendo `CustomerController.create`. Cada chamada de método no fluxo leva ao próximo arquivo da lista acima.

Exemplo de corpo esperado para essa rota:

```json
{
  "name": "Maria Silva",
  "address": "Rua A, 10",
  "city": "São Paulo",
  "country": "Brasil",
  "zipcode": "01000-000",
  "phone": "11999999999",
  "email": "maria@example.com",
  "active": true
}
```

Fluxo de erro:

```text
Service
↓
Exception
↓
GlobalExceptionHandler
↓
HTTP Response
```

Fluxo de login:

```text
POST /auth/login
↓
AuthController
↓
UserService
↓
UserRepository
↓
PasswordEncoder.matches()
↓
TokenService
↓
JWT
↓
LoginResponse
```

### 30.2 O que este exemplo simplifica

O projeto é didático e já demonstra uma arquitetura saudável, mas alguns itens seriam reforçados antes de produção:

- usar Bean Validation (`@NotBlank`, `@Email`) para enriquecer a validação de entrada;
- usar migrations como Flyway em vez de `ddl-auto=update`;
- usar um banco servidor, como PostgreSQL, quando houver múltiplas instâncias ou maior volume;
- padronizar erros em JSON e cobrir controllers/segurança com testes de integração.

Esses pontos não invalidam o aprendizado atual; eles mostram a próxima evolução natural do projeto.

---

## 31. Paralelos Java / Spring x .NET

| Java / Spring | .NET |
|---|---|
| `build.gradle` | `.csproj` |
| Gradle | MSBuild + NuGet + dotnet CLI |
| Bean | serviço registrado no DI |
| `@Service` | service class |
| `@Repository` | repository / data access |
| Filter | Middleware |
| JPA / Hibernate | Entity Framework Core |
| `JpaRepository` | Repository / DbSet |
| UUID | Guid |
| `final` | readonly |
| `application.properties` | appsettings.json |
| Spring Security | ASP.NET Authentication / Authorization |
| JUnit | xUnit / NUnit |
| Mockito | Moq |
| `@RestController` | API Controller / Minimal API handler |
| `CommandLineRunner` | lógica executada no startup |

---

## 32. Pontos para aprofundamento

O projeto cobriu uma base ampla, mas os seguintes temas merecem estudo específico:

### Spring
- Bean scopes;
- lifecycle;
- Spring Profiles;
- configuração por ambiente;
- actuator.

### JPA
- `@Transactional`;
- relacionamentos;
- `@OneToMany`;
- `@ManyToOne`;
- lazy loading;
- eager loading;
- N+1 problem;
- paginação;
- queries customizadas;
- projections.

### Banco
- PostgreSQL;
- Flyway;
- migrations;
- índices;
- transactions.

### Security
- `@PreAuthorize`;
- `@EnableMethodSecurity`;
- refresh token;
- logout;
- revogação;
- MFA;
- account lockout;
- secrets manager.

### Testes
- Mockito avançado;
- MockMvc;
- testes de integração;
- Testcontainers;
- testes de segurança;
- cobertura de código.

### Infra
- Redis;
- API Gateway;
- reverse proxy;
- Nginx;
- Cloudflare;
- observabilidade;
- métricas;
- tracing;
- health checks.

---

## 33. Checklist final

### Projeto
- [ ] Aplicação compila
- [ ] Gradle funciona
- [ ] CRUD funciona
- [ ] SQLite funciona
- [ ] DTOs estão sendo usados
- [ ] validações funcionam
- [ ] exceções globais funcionam

### Segurança
- [ ] login funciona
- [ ] senhas estão com BCrypt
- [ ] JWT é gerado
- [ ] JWT é validado
- [ ] `/auth/**` é público
- [ ] demais endpoints exigem autenticação
- [ ] USER e ADMIN têm regras distintas
- [ ] último ADMIN não pode ser removido
- [ ] CORS está configurado
- [ ] Rate Limit funciona
- [ ] secrets não estão no Git

### Testes
- [ ] testes unitários passam
- [ ] CustomerService está testado
- [ ] UserService está testado
- [ ] cenários de erro estão testados
- [ ] mocks fazem sentido
- [ ] `./gradlew test` passa

### Docker
- [ ] imagem é criada
- [ ] container sobe
- [ ] porta está publicada
- [ ] `.env` é carregado
- [ ] volume SQLite funciona
- [ ] banco persiste após recriar container

### Git
- [ ] `.env` está ignorado
- [ ] banco SQLite está ignorado
- [ ] `build/` está ignorado
- [ ] repositório está limpo
- [ ] documentação está em `docs/`

---

# Resumo mental do projeto

Se precisar lembrar a arquitetura rapidamente:

```text
Spring Boot
├── Controller
│   └── recebe HTTP
│
├── Service
│   └── regra de negócio
│
├── Validator
│   └── validações
│
├── Repository
│   └── persistência
│
├── JPA / Hibernate
│   └── ORM
│
├── SQLite
│   └── banco
│
├── Spring Security
│   ├── JWT
│   ├── Filters
│   ├── Roles
│   ├── SecurityContext
│   ├── CORS
│   └── Rate Limit
│
├── Tests
│   ├── JUnit
│   └── Mockito
│
└── Docker
    ├── Dockerfile
    ├── Compose
    ├── Environment
    └── Volume
```

---

# Referência rápida de comandos

## Gradle

```bash
./gradlew build
./gradlew test
./gradlew bootJar
```

## Java

```bash
java -version
java -jar build/libs/app.jar
```

## Git

```bash
git status
git add .
git commit -m "mensagem"
```

## Docker

```bash
docker compose up -d --build
docker compose down
docker compose ps
docker compose logs -f api
```

## Portas

```bash
sudo lsof -i :5000
ss -ltnp | grep :5000
```

---

# Conclusão

Este projeto serviu como uma introdução prática bastante completa ao ecossistema Java moderno.

Os principais conceitos assimilados foram:

- Java moderno;
- Gradle;
- estrutura de packages;
- Spring Boot;
- Dependency Injection;
- Beans;
- REST;
- DTOs;
- JPA;
- Hibernate;
- SQLite;
- validação;
- tratamento global de exceções;
- Spring Security;
- BCrypt;
- JWT;
- autenticação;
- autorização;
- roles;
- filtros;
- CORS;
- rate limiting;
- Redis como evolução futura;
- secrets;
- `.env`;
- Git;
- JUnit;
- Mockito;
- testes;
- Docker;
- persistência com volume.

A partir daqui, o maior ganho virá menos de aprender novos frameworks e mais de aprofundar conceitos como transactions, relacionamentos JPA, testes de integração, migrations, profiles, segurança avançada e arquitetura para produção.
