# BrainCoins Landing Page Redesign

**Data:** 2026-05-20  
**Arquivo alvo:** `code/frontend/moeda-estudantil/src/routes/index.tsx`

---

## Objetivo

Transformar a tela de hub de seleção de perfil em uma landing page rica e instrutiva, mantendo toda a funcionalidade existente (cards de perfil, botões demo e login/cadastro). A página deve ter aparência de produto profissional — sem referências a PUC Minas, projeto acadêmico ou nomes de autores.

---

## Estrutura da Página

A página segue o padrão **Narrativa Linear**: cada seção leva naturalmente à próxima, culminando na seleção de perfil.

```
STICKY BAR (fixa, sempre visível)
│
├── HERO
├── STATS STRIP
├── COMO FUNCIONA
├── PERFIS (tabs)
├── PARCEIROS
├── FAQ
└── CARDS DE PERFIL (âncora #profiles)
    FOOTER
```

---

## Seções Detalhadas

### 1. Sticky CTA Bar

Barra fixa na base da viewport, sempre visível durante o scroll.

- **Conteúdo:** texto curto ("BrainCoins — escolha seu perfil e comece agora") + dois botões
- **Botão primário:** "Escolher perfil ↓" → âncora `#profiles`
- **Botão secundário:** "Entrar (demo)" → entra como Aluno demo diretamente
- **Visual:** `backdrop-filter: blur`, fundo `oklch(0.10 0.025 250 / 0.85)`, borda superior amber sutil
- **Responsive:** em mobile, esconde o texto e mantém só os botões

### 2. Hero

- **Logo:** `<BrainLogo size={96}>` — imagem real `braincoins-logo.png` (moeda cerebral verde), sem emoji
- **Animação:** `animate-amber-pulse` no glow circular atrás do logo (já existe no CSS)
- **Título:** `BRAIN` + `COINS` em âmbar, fonte `font-display` tamanho `clamp(3rem, 8vw, 5.5rem)`
- **Tagline:** `font-mono`, texto "Sistema de Moeda Estudantil", `text-muted-foreground`
- **Descrição:** 2 linhas, `max-w-md`, cor `muted-foreground/70`
- **CTAs:** dois botões — "Escolher perfil ↓" (âmbar, âncora `#profiles`) + "Como funciona" (ghost, âncora `#how-it-works`)
- **Scroll cue:** indicador animado "scroll ▾" no rodapé do hero, desaparece após scroll
- **Fundo:** `.bg-dots` (já existe) + glow radial atrás do logo

### 3. Stats Strip

Faixa horizontal logo abaixo do hero, sem padding extra de seção.

| Número | Label |
|--------|-------|
| `4` | Perfis de usuário |
| `1.2k+` | Moedas em circulação |
| `12` | Empresas parceiras |
| `38` | Vantagens ativas |

- Grid de 4 colunas com divisores verticais (`border-r`)
- Números em `text-coin font-display font-black text-3xl`
- Labels em `font-mono text-xs uppercase tracking-widest text-muted-foreground`
- Fundo: `oklch(0.12 0.025 250)`, bordas top/bottom `border-border`

### 4. Como Funciona

- **Label:** badge "Como funciona"
- **Título:** "Do reconhecimento ao benefício"
- **Subtítulo:** 1 linha explicativa
- **Layout:** 4 passos em grid horizontal (`grid-cols-4`), com linha conectora central (`::before` absolute)
- **Passos:**
  1. 🏛️ Instituição distribui cotas — cor âmbar
  2. 👨‍🏫 Professor premia alunos — cor violet (`--violet`)
  3. 🎓 Aluno acumula saldo — cor emerald (`--emerald`)
  4. 🏢 Empresa confirma resgate — cor azul (`oklch(0.65 0.18 230)`)
- **Mobile:** colapsa para `grid-cols-2` (2×2), oculta linha conectora
- **Ícones:** usar componentes Lucide (`School`, `BookUser`, `GraduationCap`, `Building2`) — já importados

### 5. Perfis (Tabs)

- **Label:** badge "Perfis"
- **Título:** "Uma plataforma, quatro atores"
- **Componente:** tabs controladas por `useState` — sem dependência de biblioteca externa
- **Tabs:** Aluno / Professor / Empresa / Instituição
- **Conteúdo de cada tab:**
  - Lado esquerdo: título do perfil, descrição 2–3 linhas, lista de 4–5 funcionalidades com bullet âmbar
  - Lado direito: ícone Lucide grande em card com fundo tematizado
- **Features por perfil:**
  - **Aluno:** Saldo em tempo real, Histórico de transações, Navegar vantagens, Resgatar com cupom, Notificação por e-mail
  - **Professor:** Distribuir moedas com mensagem, Controle de saldo/cota, Histórico de envios
  - **Empresa:** Criar e gerenciar vantagens, Controle de estoque, Aprovar resgates, Notificações
  - **Instituição:** Dashboard geral, Gerenciar professores, Upload em lote, Controle de cotas

### 6. Parceiros

- **Label:** badge "Parceiros"
- **Título:** "Empresas que valorizam o mérito"
- **Layout:** `grid-cols-2 sm:grid-cols-4`
- **Cards:** empresas reais do mock (`Café Mentor`, `LivrariaTech`) + 2 slots "Sua empresa" com borda dashed e opacidade reduzida
- **Dados:** lidos diretamente de `useStore().empresas`

### 7. FAQ

- **Label:** badge "Dúvidas"
- **Título:** "Perguntas frequentes"
- **Componente:** Radix UI `Accordion` (já instalado em `src/components/ui/accordion.tsx`)
- **5 perguntas:**
  1. O que é um BrainCoin?
  2. Como resgato uma vantagem?
  3. Como me cadastro na plataforma?
  4. Minha empresa pode ser parceira?
  5. Os BrainCoins expiram?

### 8. Cards de Perfil (âncora `#profiles`)

- **ID:** `id="profiles"` para âncora do sticky bar
- **Label:** badge "Acesso"
- **Título:** "Escolha seu perfil"
- **Subtítulo:** instrução curta
- **Layout:** `grid-cols-1 sm:grid-cols-2 lg:grid-cols-4`
- **Funcionalidade preservada:** botão "Entrar (demo)" → `enterDemo(role)`, link "Login / Cadastro" → `r.path`
- **Visual:** `.vault-card` com `hover:border-coin/40` (igual ao atual, só reposicionado)

### 9. Footer

- Texto: "© 2026 BrainCoins — Todos os direitos reservados"
- Sem nomes de autores, sem menção a PUC Minas ou projeto acadêmico
- Fonte `font-mono`, cor `muted-foreground/40`

---

## Constraints de Implementação

- **Tudo em `index.tsx`** — arquivo único, sem criar novos componentes
- Usar design tokens e classes CSS já existentes (`vault-card`, `bg-dots`, `animate-amber-pulse`, `font-display`, `font-mono`, `text-coin`, etc.)
- Usar componentes já instalados: `BrainLogo` de `@/components/brand`, `Accordion` de `@/components/ui/accordion`, ícones Lucide já importados
- Logo: `<BrainLogo size={96}>` — imagem PNG real (`braincoins-logo.png`), não emoji
- Sem mencionar PUC Minas, "projeto acadêmico" ou nomes de autores em nenhum lugar
- Responsive: mobile-first, todos os grids colapsam para coluna única em `sm` ou `md`
- Funcionalidade atual (demo + login) totalmente preservada

---

## Não Incluído (fora do escopo)

- Animações de scroll (Intersection Observer) — pode ser feito depois
- Depoimentos / testimonials — descartado pelo usuário
- Contador animado nos stats — estático por ora
- Novos arquivos ou componentes separados
