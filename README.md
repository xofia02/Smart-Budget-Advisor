# Smart Budget Advisor

Trabalho de Tópicos Avançados de Programação (UCSAL). Conselheiro de orçamento de viagem: cadastro, viagens, previsão com IA, despesas, alertas, relatório, perfil e admin.

- **Backend:** Java 21, Spring Boot, PostgreSQL, JWT, Gemini — `http://localhost:8080`
- **Frontend:** React + Vite + TypeScript — `http://localhost:5173`
- **Swagger:** `http://localhost:8080/swagger-ui.html`

## Contas de demonstração

| Papel | E-mail | Senha |
|---|---|---|
| Viajante | `ana@demo.com` | `demo12345` |
| Admin | `admin@smartbudget.com` | `admin12345` |

## Como rodar

### 1. Banco (Docker)

Na raiz do repositório:

```bash
docker compose up -d
```

O compose publica o Postgres na porta **5432**. Se essa porta já estiver ocupada (PostgreSQL do Windows), suba o container na **5433**:

```bash
docker rm -f smartbudget-db
docker run -d --name smartbudget-db -e POSTGRES_DB=smartbudget -e POSTGRES_USER=smartbudget -e POSTGRES_PASSWORD=smartbudget -e TZ=America/Sao_Paulo -p 5433:5432 postgres:16-alpine
```

### 2. Backend

```bash
cd backend
copy .env.example .env
```

No `.env`, preencha `GEMINI_API_KEY` (Google AI Studio). Não commite o `.env`.

Se o banco estiver na 5433, defina no `.env` ou no terminal:

```
DB_URL=jdbc:postgresql://localhost:5433/smartbudget
DB_USER=smartbudget
DB_PASSWORD=smartbudget
```

```bash
mvn spring-boot:run
```

Confira: `http://localhost:8080/api/status` (`iaConfigurada` fica `true` com a chave).

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. A URL da API está em `frontend/.env` (`VITE_API_URL=http://localhost:8080`).

## O que o sistema faz

Viajante: cadastro (LGPD), login, perfil e histórico, CRUD de viagens, previsão e dicas de IA, chat da viagem, orçamento em 6 categorias, despesas (inclui editar e filtrar), alertas 80%/100%, bloqueio em 120%, relatório com gráfico, exclusão da conta.

Admin: indicadores anônimos e exclusão de contas de viajante (sem dados financeiros).

## Artefatos da Unidade II

- Telas (prints): [docs/prints/](docs/prints/)
- Modelo C4: [docs/c4.md](docs/c4.md)
- Roteiro do vídeo (item 15): [docs/15-roteiro-video.md](docs/15-roteiro-video.md)

## Segurança

Não versione `GEMINI_API_KEY`, `JWT_SECRET` nem o arquivo `backend/.env`. O `.env.example` só tem placeholders.
