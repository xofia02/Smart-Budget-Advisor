# Item 15 — Roteiro do vídeo individual

O arquivo `.mp4` **não vai para o GitHub** (Classroom / Overleaf). Grave a tela em `http://localhost:5173` com backend e Gemini no ar. Fale em português, 5–8 minutos.

## Antes de gravar

1. `docker` + `mvn spring-boot:run` + `npm run dev`
2. `GET /api/status` com `iaConfigurada: true`
3. Login `ana@demo.com` / `demo12345`
4. Janela em 1280×720 ou maior

## Fala e telas (nessa ordem)

| Min | Tela | O que mostrar / dizer |
|---|---|---|
| 0:00 | Login | Nome do sistema, UCSAL TAP, papéis viajante e admin |
| 0:40 | Cadastro | Consentimento LGPD (não precisa criar conta se a Ana já existir) |
| 1:00 | Início | Sistema: API, banco, IA disponível |
| 1:20 | Perfil | Estilo de viagem e histórico (previsão personalizada) |
| 1:50 | Lista / nova / editar | RF-003: criar, abrir, editar destino, excluir (só se for viagem extra) |
| 2:30 | Gramado detalhe | Previsão IA (min / recomendado / máximo) |
| 3:10 | Orçamento | Distribuir automaticamente e salvar divisão |
| 3:40 | Despesas | Lançar, **editar**, **filtrar** categoria/datas, excluir |
| 4:20 | Alertas | 80% / 100%; citar bloqueio 120% (pode usar pitoca se já estiver estourada) |
| 4:50 | Dicas + chat | Uma dica e uma pergunta curta (pode demorar) |
| 5:40 | Relatório | Planejado × gasto e barras |
| 6:10 | Sair / admin | `admin@smartbudget.com` / `admin12345`: indicadores, sem dados financeiros |
| 6:40 | Encerrar | Stack: React + Spring + Postgres + Gemini; regras no servidor |

## O que não fazer

- Não mostrar `backend/.env` nem a chave Gemini
- Não gastar o tempo no código-fonte (item 15 é o sistema rodando)
- Chat global (`POST /api/chat`) está fora do escopo da demo
