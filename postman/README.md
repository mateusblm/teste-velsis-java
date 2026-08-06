# Collection Postman — Usuários API

## Pré-requisitos

- Docker Desktop
- Postman
- Projeto executando na porta 8080

## Subir a aplicação

Na raiz do projeto:

```powershell
docker compose up --build -d
docker compose ps
```

A API ficará disponível em `http://localhost:8080`.

## Importar no Postman

1. Importe `usuarios-api.postman_collection.json`.
2. Importe `usuarios-api.local.postman_environment.json`.
3. Selecione o environment **Usuários API - Local**.
4. Execute as requisições na ordem apresentada.

A collection cria um e-mail único no pre-request do cadastro usando `Date.now()`. O ID retornado é armazenado em `usuarioId`. As variáveis de collection são `baseUrl`, `usuarioId`, `emailTeste`, `senhaAtual` e `novaSenha`.

## Collection Runner

1. Clique com o botão direito na collection.
2. Selecione **Run collection**.
3. Mantenha a ordem das requisições.
4. Use uma única iteração.

## Newman

Instale, se necessário:

```powershell
npm install -g newman
```

Execute:

```powershell
newman run postman/usuarios-api.postman_collection.json -e postman/usuarios-api.local.postman_environment.json
```

## Cenários cobertos

- cadastro público válido e ausência da senha na resposta;
- acesso sem autenticação e senha incorreta;
- consulta autenticada por ID e usuário inexistente;
- validação de nome, e-mail e senha;
- e-mail duplicado;
- atualização sem alteração de senha;
- confirmação da senha antiga;
- alteração e validação da nova senha;
- paginação;
- filtro parcial e case-insensitive.

