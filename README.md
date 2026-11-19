# Smart Budget Advisor

Assistente de orçamento de viagens com Inteligência Artificial. O viajante cadastra a viagem e o orçamento, e o sistema usa o Google Gemini para prever os gastos por categoria, dar dicas de economia e responder dúvidas num chat. Durante a viagem, ele registra as despesas e recebe alertas quando uma categoria chega a 80% e a 100% do planejado.

Projeto da disciplina **Tópicos Avançados em Programação**, UCSAL, 2026.2, com o Prof. Elton Figueiredo da Silva.

## Time

| Integrante | Papel |
| --- | --- |
| Sofia Santos de Mendonça | Scrum Master e documentação |
| Loren Vitória | Product Owner e front-end |
| Flavio Cerqueira | Back-end e Trello |
| João Pedro Santana Sousa | Protótipo no Figma, front-end e vídeo |
| Cauã Maia | Modelo C4 e front-end |

## Links

- Trello: https://trello.com/b/KfxgHYcT
- Protótipo no Figma: https://www.figma.com/design/ji5v5m9Ria9kmeyorOyJXH/Smart-Budget-Adivisor
- Documentação da API (com o back-end rodando): http://localhost:8080/swagger-ui.html

## Tecnologias

| Parte | Tecnologia |
| --- | --- |
| Back-end | Java 21, Spring Boot 3.5, Spring Security com JWT, Spring Data JPA |
| Banco de dados | PostgreSQL 16 (via Docker) |
| Inteligência Artificial | API do Google Gemini |
| Front-end | React |
| Documentação da API | Swagger (springdoc) |
| Testes | JUnit |

## Estrutura do repositório

| Pasta | O que tem |
| --- | --- |
| `backend/` | Servidor em Java com Spring Boot |
| `frontend/` | Telas em React |
| `docs/` | Diagramas e documentos do projeto |
| `docker-compose.yml` | Banco de dados PostgreSQL pronto para rodar |

## Como rodar o back-end

**Você vai precisar de:** Java 21, Maven e Docker.

1. Suba o banco de dados (na raiz do projeto):

   ```bash
   docker compose up -d
   ```

2. Crie o arquivo de configuração a partir do modelo e coloque a sua chave do Gemini em `GEMINI_API_KEY`:

   ```bash
   cp backend/.env.example backend/.env
   ```

   A chave é gerada de graça em https://aistudio.google.com/apikey.

3. Rode o servidor:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. Confira se está tudo certo:
   - http://localhost:8080/api/status mostra se o servidor está no ar;
   - http://localhost:8080/api/teste/gemini manda uma pergunta de teste para a IA;
   - http://localhost:8080/swagger-ui.html abre todos os endpoints para testar.

> **Nunca envie o arquivo `.env` para o GitHub.** Ele guarda a chave da IA e as senhas e já está no `.gitignore`. O repositório guarda só o modelo `.env.example`, sem a chave.

### Variáveis do `.env`

| Variável | Para que serve |
| --- | --- |
| `GEMINI_API_KEY` | Chave da API do Gemini (obrigatória para as funções de IA) |
| `GEMINI_MODEL` | Modelo do Gemini usado |
| `JWT_SECRET` | Segredo que assina o token de login (mínimo de 32 caracteres) |
| `ADMIN_EMAIL` e `ADMIN_SENHA` | Conta do administrador criada ao iniciar |
| `DADOS_DEMO` | `true` cria a usuária de demonstração |
| `DB_URL`, `DB_USER` e `DB_PASSWORD` | Acesso ao banco (já batem com o `docker-compose.yml`) |
| `FRONTEND_URL` | Endereço do front-end, liberado para chamar a API |

## Contas de demonstração

Com `DADOS_DEMO=true`, o sistema cria estas contas ao iniciar:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Viajante (Ana Souza, com 2 viagens anteriores) | `ana@demo.com` | `demo12345` |
| Administrador | `admin@smartbudget.com` | `admin12345` |

São senhas só para teste local. Em outro ambiente, troque-as no `.env`.

## Endpoints principais

Todos começam com `/api`. Fora cadastro, login e status, é preciso mandar o token recebido no login no cabeçalho `Authorization: Bearer <token>`.

| Grupo | Endereço | O que permite |
| --- | --- | --- |
| Autenticação | `/auth/cadastro`, `/auth/login`, `/auth/logout` | Criar conta, entrar e sair |
| Conta | `DELETE /usuarios/me` | Excluir a própria conta (LGPD) |
| Perfil | `/perfil`, `/perfil/viagens-anteriores` | Estilo de viagem e viagens anteriores |
| Viagens | `/viagens`, `/viagens/{id}` | Criar, listar, editar e excluir viagens |
| Despesas | `/viagens/{id}/despesas` | Registrar, editar, excluir e filtrar despesas por categoria e data |
| Alertas | `/viagens/{id}/alertas` | Ver os alertas de 80% e 100% |
| IA | `/viagens/{id}/previsao`, `/viagens/{id}/dicas`, `/viagens/{id}/chat`, `/chat` | Previsão de gastos, dicas e chat com o assistente |
| Orçamento | `/viagens/{id}/orcamento/distribuir`, `/viagens/{id}/orcamento` | Dividir e ajustar o orçamento por categoria |
| Relatório | `/viagens/{id}/relatorio` | Comparar o planejado com o gasto |
| Administrador | `/admin/usuarios`, `/admin/indicadores` | Listar e excluir contas, ver indicadores anônimos |

## Regras de negócio no servidor

- Orçamento mínimo de R$ 500,00 e viagem de 2 a 365 dias.
- Toda viagem nova recebe as 6 categorias fixas, e a soma delas não passa do orçamento.
- A despesa precisa ter data dentro do período da viagem.
- Alertas quando uma categoria chega a 80% e passa de 100%.
- Acima de 120% do orçamento, novas despesas ficam bloqueadas até o viajante revisar o orçamento.
- Antes de ir para a IA, nome, e-mail e outros dados pessoais são retirados. A IA tem até 8 segundos para responder; se falhar, o resto do sistema continua funcionando.

## Testes

```bash
cd backend
mvn test
```

Os testes cobrem a validação da viagem, os alertas por categoria e a divisão do orçamento.

## Como rodar o front-end

Em construção. O front fica na pasta `frontend/` e roda em http://localhost:5173.

## Diagramas

Ficam na pasta `docs/`:

| Arquivo | O que mostra |
| --- | --- |
| `diagrama_caso_de_uso.svg` | Casos de uso do viajante e do administrador |
| `diagrama_classes.png` | Classes do sistema (base do pacote `model`) |
| `bpmn_parte1.pdf` e `bpmn_parte2.pdf` | Processo completo da viagem, do cadastro ao relatório (BPMN) |
| `smart-budget-advisor-e2e.bpmn` | Arquivo editável do BPMN (abre no Camunda Modeler ou em bpmn.io) |

