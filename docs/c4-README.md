# Figuras C4 (item 12)

Três PNGs para o GitHub / Overleaf. **Não substituem** as figuras Graphviz que já estão no documento da equipe, a menos que a liderança peça troca.

| Arquivo | Nível |
|---|---|
| [c4-contexto.png](c4-contexto.png) | 1 — contexto |
| [c4-conteineres.png](c4-conteineres.png) | 2 — contêineres |
| [c4-componentes.png](c4-componentes.png) | 3 — componentes da API |

Nível 4 no documento TAP = diagrama de classes da seção 8 (não é esta pasta).

Fonte em texto: [c4.md](c4.md). Recriar PNG:

```bash
npx --yes @mermaid-js/mermaid-cli -i docs/c4-contexto.mmd -o docs/c4-contexto.png -b white
npx --yes @mermaid-js/mermaid-cli -i docs/c4-conteineres.mmd -o docs/c4-conteineres.png -b white
npx --yes @mermaid-js/mermaid-cli -i docs/c4-componentes.mmd -o docs/c4-componentes.png -b white
```
