# EventosTec API

API REST para gerenciamento de eventos de tecnologia (presenciais e remotos), com cupons de desconto, upload de imagens no S3 e listagem paginada/filtrada. Projeto desenvolvido acompanhando o curso de Java + Spring Boot da Fernanda Kipper.

> ℹ️ O código-fonte do projeto fica na subpasta [`eventostec/`](eventostec/). Os comandos Maven devem ser executados a partir dela.

## 🔗 Aplicação no ar

A API está publicada na AWS (EC2 + RDS PostgreSQL), com IP fixo (Elastic IP):

- **Listagem de eventos:** http://18.222.42.33:8080/api/event?page=0&size=12
- **Filtro (só remotos):** http://18.222.42.33:8080/api/event/filter?remote=true&size=100
- **Detalhes de um evento:** `http://18.222.42.33:8080/api/event/{id}`

## 🛠️ Stack

- **Java 21** + **Spring Boot 3.3**
- **Spring Data JPA** / Hibernate
- **PostgreSQL 15** (Docker no dev, Amazon RDS em produção)
- **Flyway** (dono do schema; `ddl-auto=validate`)
- **AWS**: S3 (imagens), RDS (banco), EC2 (deploy)

## 📁 Estrutura

```
api/                         ← raiz do repositório
├── eventostec/              ← projeto Spring Boot (pom.xml aqui)
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.properties          (base + profile padrão)
│   │   ├── application-local.properties     (dev: Docker)
│   │   ├── application-test.properties      (testes: Docker)
│   │   └── application-prod.properties       (produção: RDS via env vars)
│   └── seed-eventos-demo.sql                (dados de exemplo)
└── README.md
```

## ▶️ Rodando localmente

**Pré-requisitos:** JDK 21, Maven, Docker.

1. Suba um PostgreSQL local na porta `5434`:
   ```bash
   docker run -d --name eventostec_postgres \
     -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=eventostec -p 5434:5432 postgres:15
   ```
2. Rode a aplicação (usa o profile `local` por padrão):
   ```bash
   cd eventostec
   mvn spring-boot:run
   ```
   O Flyway cria o schema automaticamente no start. A API sobe em `http://localhost:8080`.

3. (Opcional) Popular com dados de demo:
   ```bash
   docker exec -i eventostec_postgres psql -U postgres -d eventostec < seed-eventos-demo.sql
   ```

## ⚙️ Profiles

O ambiente é controlado por profiles do Spring:

| Profile | Arquivo | Banco |
|---------|---------|-------|
| `local` (padrão) | `application-local.properties` | Docker (`localhost:5434`) |
| `test` | `application-test.properties` | Docker (`localhost:5434`) |
| `prod` | `application-prod.properties` | Amazon RDS (via variáveis de ambiente) |

Para trocar de profile: `SPRING_PROFILES_ACTIVE=prod` (variável de ambiente) ou `--spring.profiles.active=prod` (argumento).

## 🌐 Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/event` | Cria um evento (`multipart/form-data`) |
| `GET`  | `/api/event?page=0&size=12` | Lista eventos futuros (paginado) |
| `GET`  | `/api/event/filter?title=&city=&uf=&remote=&startDate=&endDate=` | Filtra eventos futuros |
| `GET`  | `/api/event/{id}` | Detalhes de um evento (com cupons) |
| `POST` | `/api/coupon/event/{eventId}` | Cadastra um cupom para o evento (JSON) |

## ☁️ Deploy (AWS EC2 + RDS)

Arquitetura em produção:

- **EC2** (Amazon Linux 2023) rodando o `.jar` como serviço **systemd** (`eventostec`) — com auto-start no boot e reinício automático em caso de falha.
- **Elastic IP** fixo (`18.222.42.33`) — o endereço público não muda ao parar/ligar a instância.
- **RDS PostgreSQL** privado, na mesma VPC da EC2 (acessível apenas pela rede interna).
- Credenciais do banco em `/etc/eventostec.env` na EC2 (fora do jar e do Git); a senha **nunca** é versionada.

### Publicar uma nova versão (redeploy)

```bash
# 1. Gerar o .jar (pulando os testes, que exigem o banco)
cd eventostec
mvn clean package -DskipTests

# 2. Copiar para a EC2
scp -i ~/.ssh/eventostec-key.pem target/api-0.0.1-SNAPSHOT.jar ec2-user@18.222.42.33:~/app.jar

# 3. Reiniciar o serviço
ssh -i ~/.ssh/eventostec-key.pem ec2-user@18.222.42.33 "sudo systemctl restart eventostec"
```

Comandos úteis na EC2: `sudo systemctl status eventostec` · `sudo journalctl -u eventostec -f`.

Existem várias formas de fornecer a configuração de produção (variáveis de ambiente, argumentos de linha de comando, `-D`, arquivo externo ao lado do jar) — todas documentadas em [`application-prod.properties`](eventostec/src/main/resources/application-prod.properties).

## 🌿 Fluxo de branches

- `main` — releases estáveis (nunca commitar direto)
- `develop` — integração
- `feature/*` / `fix/*` — trabalho novo, criado a partir de `develop`

Fluxo: `feature/x` → PR para `develop` → após merge, PR `develop` → `main` (release).
