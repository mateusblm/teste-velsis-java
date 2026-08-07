# Usuários API

API REST para cadastro, consulta e atualização de usuários. 

## Tecnologias utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL 16
- H2 para os testes
- Docker Compose
- OpenAPI e Swagger UI

## Estrutura principal

```text
.
├── .github
│   └── workflows
│       └── tests.yml
├── postman
│   ├── README.md
│   ├── usuarios-api.local.postman_environment.json
│   └── usuarios-api.postman_collection.json
├── src
│   ├── main
│   │   ├── java/com/mateusburlamaqui/usuarios
│   │   │   ├── documentacao
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── email
│   │   │   │   └── EmailService.java
│   │   │   ├── excecao
│   │   │   │   ├── ErroResponse.java
│   │   │   │   └── TratadorGlobalException.java
│   │   │   ├── seguranca
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── UsuarioDetailsService.java
│   │   │   ├── usuario
│   │   │   │   ├── dto
│   │   │   │   │   ├── AtualizarUsuarioRequest.java
│   │   │   │   │   ├── UsuarioRequest.java
│   │   │   │   │   └── UsuarioResponse.java
│   │   │   │   ├── excecao
│   │   │   │   │   ├── EmailJaCadastradoException.java
│   │   │   │   │   └── UsuarioNaoEncontradoException.java
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── UsuarioController.java
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   └── UsuarioService.java
│   │   │   └── UsuariosApiApplication.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java/com/mateusburlamaqui/usuarios
│       │   ├── seguranca/SegurancaIntegracaoTest.java
│       │   ├── usuario/UsuarioControllerTest.java
│       │   ├── usuario/UsuarioServiceTest.java
│       │   ├── usuario/UsuarioServiceIntegracaoTest.java
│       │   └── UsuariosApiApplicationTests.java
│       └── resources
│           └── application-test.properties
├── compose.yaml
├── Dockerfile
├── pom.xml
```

## Decisões do projeto

### Separação por responsabilidades

O controller recebe as requisições HTTP e devolve as respostas. O service concentra as regras de negócio, como normalização de e-mail, validação de duplicidade e alteração de senha. O repository fica responsável pelo acesso ao banco.

### Segurança

A API usa HTTP Basic. O e-mail do usuário é utilizado como nome de usuário e a senha é comparada com o hash armazenado no banco.

O cadastro é público porque um usuário ainda não possui credenciais antes de ser criado. Consultas e atualizações exigem autenticação.

As senhas são armazenadas usando hash BCrypt antes da persistência.

O escopo do desafio não define perfis ou níveis de permissão, portanto a segurança implementada trata autenticação dos usuários, sem regras adicionais de autorização por perfil.

### Persistência e envio de e-mail

No cadastro e na atualização, a persistência é realizada com `saveAndFlush` antes do envio do e-mail simulado. Dessa forma, erros de persistência são identificados antes de informar que o e-mail foi enviado.

Os métodos são anotados com `@Transactional`. Caso o `EmailService` lance uma exceção não tratada durante a operação, a transação é revertida e a alteração no banco não é confirmada.

Como no desafio o envio de e-mail é apenas simulado no console preferi essa alternativa. Em um cenário real com um serviço externo de e-mail, banco de dados e provedor
de e-mail não participariam da mesma transação. Uma alternativa para maior confiabilidade seria utilizar o padrão Transactional Outbox e processamento assíncrono.

### Banco de dados

O Docker Compose executa tanto o PostgreSQL quanto a aplicação Spring Boot. A API é construída pelo `Dockerfile` e iniciada no container `app`, enquanto o banco é iniciado no container `postgres`.

Os testes usam H2 em memória para serem rápidos e independentes de um banco instalado na máquina. Por isso, não é necessário iniciar o PostgreSQL para executar mvn test.

## Como executar com Docker

Pré-requisitos:

- Docker Desktop

Na raiz do projeto, execute:

```powershell
docker compose up --build -d
```

Esse comando constrói a imagem da API Spring Boot e sobe os dois serviços: `app` e `postgres`. Portanto, não é necessário ter Java, Maven ou PostgreSQL instalados na máquina para executar a aplicação dessa forma.

Verifique o estado dos serviços:

```powershell
docker compose ps
```

O resultado esperado é:

- `postgres` como `healthy`;
- `app` como `healthy`;
- API publicada na porta `8080`.

Para acompanhar os logs:

```powershell
docker compose logs -f app
docker compose logs -f postgres
```

Para parar os containers sem apagar os dados:

```powershell
docker compose down
```

O comando abaixo também remove o volume do PostgreSQL e apaga os dados locais:

```powershell
docker compose down -v
```

## Como executar localmente

Nesta opção, a aplicação Spring Boot é executada diretamente na máquina pelo Maven. O PostgreSQL precisa estar disponível separadamente, seja instalado localmente ou iniciado apenas pelo serviço `postgres` do Docker Compose.

Com o PostgreSQL disponível, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrão, a aplicação usa:

```text
URL: jdbc:postgresql://localhost:5432/usuarios
Usuário: usuarios
Senha: usuarios
```

## Testes automatizados

Execute a suíte completa com:

```powershell
.\mvnw.cmd test
```

Os testes usam H2 e cobrem:

- cadastro de usuário;
- criptografia da senha;
- usuário inexistente;
- e-mail duplicado;
- atualização com e sem nova senha;
- rollback da transação quando o envio de e-mail falha;
- filtro por nome;
- paginação;
- respostas do controller;
- cadastro público;
- acesso sem credenciais;
- senha incorreta;
- autenticação válida.

Os cenários foram escolhidos por cobrirem as regras de negócio mais sensíveis da aplicação como integridade dos dados, segurança das credenciais, atualização de senha, consulta paginada, atomicidade da persistência e tratamento adequado dos principais erros retornados ao cliente. Os testes de service validam as regras de negócio de forma isolada, os testes de controller verificam os contratos HTTP, e os testes de integração confirmam as regras de segurança e o rollback da transação quando o envio de e-mail falha.

## Endpoints

### Cadastrar usuário

`POST /api/usuarios`

Não exige autenticação.

```json
{
  "nome": "Mateus Burlamaqui",
  "email": "mateus@example.com",
  "senha": "senha123"
}
```

Resposta: `201 Created`

```json
{
  "id": 1,
  "nome": "Mateus Burlamaqui",
  "email": "mateus@example.com"
}
```

O campo `senha` não é devolvido.

### Listar usuários

`GET /api/usuarios`

Exige HTTP Basic.

Parâmetros disponíveis:

- `page`: número da página, começando em `0`;
- `size`: quantidade de itens por página;
- `sort`: campo e direção, por exemplo `nome,asc`;
- `nome`: filtro opcional por qualquer parte do nome, sem diferença entre maiúsculas e minúsculas.

Exemplo:

```text
GET /api/usuarios?page=0&size=5&sort=nome,asc
```

Filtro:

```text
GET /api/usuarios?nome=burlamaqui
```

### Buscar usuário por ID

`GET /api/usuarios/{id}`

Exige HTTP Basic.

Resposta: `200 OK` quando o usuário existe ou `404 Not Found` quando não existe.

### Atualizar usuário

`PUT /api/usuarios/{id}`

Exige HTTP Basic.

```json
{
  "nome": "Mateus Burlamaqui Moreira",
  "email": "mateus@example.com",
  "senha": null
}
```

Quando `senha` é `null`, a senha atual é mantida. Para trocar a senha, envie uma nova senha com pelo menos 8 caracteres.

A atualização retorna `409 Conflict` quando o e-mail informado pertence a outro usuário.

## Formato dos erros

Os erros tratados pela aplicação seguem este formato:

```json
{
  "dataHora": "2026-08-06T22:51:15.931490404",
  "status": 400,
  "mensagem": "Dados inválidos.",
  "campos": {
    "nome": "O nome é obrigatório.",
    "email": "O e-mail informado é inválido.",
    "senha": "A senha deve ter entre 8 e 100 caracteres."
  }
}
```

## Swagger / OpenAPI

Com a aplicação em execução, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI está disponível em:

```text
http://localhost:8080/v3/api-docs
```

O cadastro não exige autenticação, os demais endpoints documentados exigem as credenciais do usuário.

## Healthcheck

O endpoint público de saúde da aplicação é:

```text
GET /actuator/health
```

O Docker Compose usa esse endpoint para marcar o serviço `app` como saudável. O PostgreSQL também possui um healthcheck próprio, e a API só inicia depois que o banco está pronto.

## Postman e Newman

Os arquivos estão na pasta `postman/`:

- `usuarios-api.postman_collection.json`;
- `usuarios-api.local.postman_environment.json`.

A collection gera um e-mail único antes do cadastro, captura o ID retornado e executa os cenários em sequência, incluindo autenticação, validação, atualização de senha, paginação e filtros.

Para executar com Newman:

```powershell
npx --yes newman run postman/usuarios-api.postman_collection.json -e postman/usuarios-api.local.postman_environment.json
```

## GitHub Actions

O workflow `.github/workflows/tests.yml` executa os testes em cada push e pull request usando Java 21. Como a suíte usa H2, o workflow não precisa iniciar o PostgreSQL.

## Possibilidades para o frontend

Pensei principalmente em Angular, React e Vue para o frontend.

Minha escolha seria o **Angular**, principalmente porque já tenho mais familiaridade com a tecnologia e porque ele já oferece recursos que fazem sentido para esse tipo de aplicação, como roteamento, formulários reativos, validação, `HttpClient` e injeção de dependência.

Para uma interface com cadastro, login, listagem, filtros e edição de usuários, essa estrutura ajuda a manter o projeto organizado desde o início.

Como ponto negativo, o Angular possui mais conceitos e mais estrutura inicial, então em uma aplicação muito pequena ele pode acabar sendo mais pesado do que o necessário.

O **React** também seria uma ótima alternativa. Ele tem um ecossistema muito grande, bastante material disponível e muita flexibilidade para montar a aplicação. Ao mesmo tempo, essa liberdade exige mais decisões sobre organização do projeto, gerenciamento de estado, formulários e escolha de bibliotecas.

O **Vue** também seria uma opção interessante, principalmente pela simplicidade e pela curva de aprendizado menor. Para projetos pequenos ou médios, permite desenvolver rápido e manter uma estrutura limpa. No meu caso, porém, tenho menos experiência com Vue do que com Angular, apesar de já ter trabalhado com a tecnologia.

Por isso, se eu fosse continuar o desenvolvimento do frontend deste projeto, escolheria **Angular**. Não por considerar Angular melhor em todos os cenários, mas porque ele se encaixa bem nas necessidades desta aplicação e é a tecnologia com a qual eu teria mais segurança para desenvolver e manter o projeto.

No frontend eu utilizaria:

- componentes Angular para as telas;
- Reactive Forms para cadastro e edição;
- HttpClient para comunicação com a API;
- um service para centralizar as requisições;
- um interceptor para adicionar o cabeçalho HTTP Basic;
- guards para controlar o acesso às telas protegidas.
