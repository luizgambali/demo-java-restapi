# Demo API REST

Exemplo de API REST em **Java 25** com **Spring Boot**, Gradle e SQLite. O projeto demonstra cadastro de clientes e usuários, autenticação stateless com JWT, controle de acesso por papéis e execução em Docker.

## Recursos principais

- API REST para clientes (`/customers`), com validação de nome e e-mail.
- Cadastro, login e alteração de senha de usuários.
- Autenticação por JWT e senhas protegidas com BCrypt.
- Papéis `USER` e `ADMIN`; operações administrativas exigem token de administrador.
- Limite de cinco tentativas de login por minuto para cada IP.
- Banco SQLite persistido em volume Docker.
- Configurações sensíveis fornecidas por variáveis de ambiente — nenhum segredo deve ser versionado.

## Requisitos

- Docker e Docker Compose para executar em contêiner; ou
- JDK 25 para executar localmente. O Gradle Wrapper já está incluso.

## Executar com Docker

1. Crie a configuração local a partir do modelo:

   ```bash
   cp .env.example .env
   ```

2. Edite `.env` e informe um segredo JWT seguro. Você pode gerar um com:

   ```bash
   openssl rand -base64 48
   ```

   `JWT_SECRET` é obrigatório e deve ter ao menos 32 bytes. `ADMIN_USERNAME` e `ADMIN_PASSWORD` são opcionais; se ambos forem preenchidos, um administrador inicial será criado apenas quando ainda não houver administrador no banco.

3. Inicie a aplicação:

   ```bash
   docker compose up --build -d
   ```

   A API ficará disponível em `http://localhost:5000`.

4. Consulte os logs, se necessário:

   ```bash
   docker compose logs -f api
   ```

### Parar e remover

Para parar e remover o contêiner, preservando o banco de dados:

```bash
docker compose down
```

Para remover também o volume SQLite e todos os dados locais — ação irreversível:

```bash
docker compose down -v
```

## Executar localmente

Defina o segredo JWT e execute pelo Gradle Wrapper:

```bash
export JWT_SECRET="cole-um-segredo-aleatorio-com-ao-menos-32-bytes"
./gradlew bootRun
```

Por padrão, o banco local é `customers.db`. Para trocar seu local, defina `DB_URL`, por exemplo `DB_URL=jdbc:sqlite:/caminho/para/demo.db`.

## Executar no IntelliJ IDEA (sem Docker)

1. Crie o arquivo local de variáveis a partir do modelo:

   ```bash
   cp .env.example .env
   ```

   Preencha ao menos `JWT_SECRET`. O arquivo `.env` é ignorado pelo Git e não deve ser versionado.

2. Abra a pasta do projeto no IntelliJ IDEA e importe-o como projeto **Gradle**.

3. Caso o suporte a arquivos `.env` não esteja disponível, instale o plugin **EnvFile** em **Settings/Preferences > Plugins**.

4. Abra **Run > Edit Configurations...**, crie uma configuração **Spring Boot** e escolha a classe principal `com.example.demo.MainApp`.

5. Na seção/aba **EnvFile** da configuração, marque **Enable EnvFile** e adicione o arquivo `$PROJECT_DIR$/.env`. Mantenha o diretório de trabalho como a raiz do projeto.

6. Clique em **Run**. A aplicação inicia em `http://localhost:5000`, usando `customers.db` na raiz do projeto, a menos que `DB_URL` seja definido no `.env`.

Se preferir não usar o plugin, copie as variáveis de `.env` para o campo **Environment variables** da configuração de execução. Não inclua valores reais dessas variáveis nos arquivos do projeto.

## Endpoints

O arquivo [demo-api.postman_collection.json](demo-api.postman_collection.json) contém todos os endpoints e exemplos de requisição. Importe-o no Postman e execute **Autenticação > Login**: o token retornado é salvo automaticamente na variável da coleção `token`.

| Método | Endpoint | Autorização | Descrição |
| --- | --- | --- | --- |
| POST | `/auth/register` | Pública | Cria usuário comum |
| POST | `/auth/login` | Pública | Retorna token JWT |
| GET, POST, PUT, DELETE | `/customers` | JWT | Gerencia clientes |
| GET | `/users/get-by-id/{id}` | JWT | Consulta o próprio usuário; admin consulta qualquer usuário |
| POST | `/users/change-password` | JWT | Altera senha |
| GET | `/users/get-all` | Admin | Lista usuários |
| POST | `/users/create-admin` | Admin | Cria administrador |
| DELETE | `/users/delete/{id}` | Admin | Exclui usuário |

Envie o JWT no cabeçalho:

```text
Authorization: Bearer <token>
```

## Segurança e arquivos locais

`.env`, bancos SQLite, certificados, chaves e artefatos de build estão no `.gitignore`. Não versione `.env` nem `customers.db`: o banco pode conter dados pessoais e hashes de senha. Para publicar o projeto, mantenha apenas `.env.example` como referência de configuração.
