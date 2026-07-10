# Login Auth API

API de autenticação com JWT (registro, login e rota protegida), desenvolvida acompanhando o Projeto 2 do curso de Java + Spring Boot da Fernanda Kipper.

## 🛠️ Stack

- **Java 17** + **Spring Boot 3.5**
- **Spring Security** — filtro customizado (`OncePerRequestFilter`) validando o token a cada request
- **Auth0 java-jwt** — geração e validação dos tokens (HMAC256)
- **Spring Data JPA** + **H2** (banco em memória)
- **Lombok**

## ▶️ Rodando localmente

**Pré-requisitos:** JDK 17+, Maven.

```bash
cd login-auth-api
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

> ⚠️ O banco H2 é em memória: os usuários registrados são perdidos a cada restart da aplicação.

## 🌐 Endpoints

| Método | Rota | Autenticação | Descrição |
|--------|------|--------------|-----------|
| `POST` | `/auth/register` | pública | Cria usuário e retorna nome + token JWT |
| `POST` | `/auth/login` | pública | Autentica e retorna nome + token JWT |
| `GET`  | `/user` | Bearer token | Rota protegida de exemplo |

### Exemplos

```bash
# Registro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Fulano","email":"fulano@email.com","password":"123456"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"fulano@email.com","password":"123456"}'

# Rota protegida (usar o token retornado acima)
curl http://localhost:8080/user -H "Authorization: Bearer <token>"
```

## 🔐 Como funciona a autenticação

1. `/auth/register` salva o usuário com a senha criptografada (BCrypt) e já devolve um token.
2. `/auth/login` compara a senha com `passwordEncoder.matches` e devolve um token novo.
3. O token JWT (HMAC256, expiração de 2h) carrega o e-mail do usuário no `subject`.
4. Em toda request, o `SecurityFilter` extrai o token do header `Authorization`, valida a assinatura, busca o usuário no banco e popula o `SecurityContextHolder`.
5. Qualquer rota fora de `/auth/*` exige token válido (`anyRequest().authenticated()`).

> O segredo do token (`api.security.token.secret`) está no `application.properties` com um valor de exemplo do curso — em um projeto real, viria de variável de ambiente e nunca seria versionado.
