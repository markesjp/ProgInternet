
# Relatório Técnico (MD) — JSP Reutilizável + Fluxo Servlet/JSP (forward/include) + Upload/Download por Usuário
**Projeto:** Trabalho5 / Autoescola (Servlet + JSP + Tomcat)  
**Objetivo do relatório:** demonstrar **onde** e **como** os requisitos pedidos estão implementados, explicando **para que serve**, **como funciona** e o **fluxo completo** (sucesso/erro) — com foco em **header/footer/home** e no requisito de **upload/download (2 arquivos por usuário)**.

---

## 1) Visão Geral dos Requisitos e Como o Projeto Atende

### R1 — Componentes reusáveis em JSP + `<jsp:include>`
**Requisito:** Criar pelo menos **dois componentes reusáveis** (ex.: `header.jsp`, `footer.jsp`) e integrar nas páginas principais usando `<jsp:include>`.

**Como atender no projeto (proposta padrão):**
- Criar:
  - `/WEB-INF/jsp/components/header.jsp`
  - `/WEB-INF/jsp/components/footer.jsp`
- Em **home.jsp** (e demais páginas principais), incluir:
  - `<jsp:include page="/WEB-INF/jsp/components/header.jsp" />`
  - `<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />`

**Benefício:** padroniza layout, evita duplicação de HTML, facilita manutenção (alterar o cabeçalho/rodapé em um único lugar).

> Neste relatório, a explicação de JSP reutilizável fica limitada a **header**, **footer** e **home** (conforme solicitado).

---

### R2 — Um Servlet processa requisição e encaminha com `<jsp:forward>` (sucesso/erro)
**Requisito:** Dado login/cadastro/busca existentes, usar um **Servlet** para processar e encaminhar para **JSP distintas** usando `<jsp:forward>` dependendo de **sucesso ou erro**.

**Como atender no projeto (exemplo típico com Login):**
- `LoginServlet` (ou o servlet equivalente que você já tem)
  - Valida credenciais
  - Se OK → `forward` para `home.jsp` (ou `home_sucesso.jsp`)
  - Se ERRO → `forward` para `login.jsp` (ou `login_erro.jsp`) com mensagem via `request.setAttribute(...)`

**Ponto-chave:**  
- `<jsp:forward>` mantém **request** e **atributos**, e não troca a URL no browser (diferente de redirect).
- Ideal para mostrar mensagens e dados de erro/sucesso sem perder contexto.

---

### R3 — “Qualquer usuário deve fazer upload de 2 arquivos e poderá baixá-los em seus respectivos contextos”
**Requisito (alteração de sala):**
- “Qualquer usuário no sistema deve fazer upload de **dois arquivos**, e poderá baixá-los em seus respectivos contextos.”

**Interpretação operacional para o projeto:**
- “Contexto do usuário” = **seus arquivos**, não os de outros usuários.
- O sistema deve:
  1) identificar o usuário (logado ou algum identificador equivalente)
  2) permitir upload **apenas até 2 arquivos**
  3) listar apenas os arquivos daquele usuário
  4) permitir download apenas desses arquivos

> Observação importante (arquitetura):  
> Apenas “guardar na sessão” não atende bem ao requisito de “contexto por usuário” de forma consistente, porque sessão expira e o usuário perde o vínculo.
> O ideal é persistir metadados em banco **ou** organizar em pastas por usuário e manter um índice mínimo (banco/arquivo JSON).  
> Porém, como seu projeto já tem o fluxo em sessão, o ajuste mínimo para atender requisito de “por usuário” é: **usar pasta por usuário** + **lista na sessão daquele usuário**.

---

## 2) Estrutura JSP Reutilizável (Header, Footer e Home)

### 2.1 `header.jsp` — Cabeçalho Reutilizável
**Local recomendado:** `/WEB-INF/jsp/components/header.jsp`  
**Função:** conter elementos repetidos no topo das telas:
- `<head>` (ou parte dele)
- links CSS (Bootstrap, CSS local)
- topo visual (logo, título)
- navegação “Home / Upload / Aulas / Alunos” (se existir)

**Como funciona:**
- É incluído nas páginas principais com:
  ```jsp
  <jsp:include page="/WEB-INF/jsp/components/header.jsp" />


* Quando o JSP principal é renderizado, o container “injeta” o HTML do header naquele ponto.

---

### 2.2 `footer.jsp` — Rodapé Reutilizável

**Local recomendado:** `/WEB-INF/jsp/components/footer.jsp`
**Função:** padronizar:

* rodapé visual
* scripts JS (Bootstrap bundle)
* informações do sistema (versão, ano, etc.)

**Como funciona:**

* Incluído no final das páginas principais com:

  ```jsp
  <jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
  ```

---

### 2.3 `home.jsp` — Página Principal (Integra Header/Footer)

**Local recomendado:** `/WEB-INF/jsp/pages/home.jsp`
**Função:** ser o hub do sistema:

* exibir mensagens de sucesso/erro (vindas de servlets via `request` ou querystring)
* apresentar ações principais: Upload, Download/listagem, Aulas, Alunos, etc.

**Como funciona (layout):**

1. inclui `header.jsp`
2. mostra conteúdo central
3. inclui `footer.jsp`

**Fluxo típico:**

* Servlets redirecionam/forward para home
* Home lê `request.getAttribute("msg")` / `("erro")` (ou parâmetros) e exibe alertas.

---

## 3) Fluxo Servlet → JSP com `<jsp:forward>` (Sucesso/Erro)

### 3.1 Regra do requisito

Um servlet deve processar a requisição e, conforme resultado:

* **Sucesso**: encaminhar para uma JSP de sucesso
* **Erro**: encaminhar para uma JSP de erro (ou a mesma com bloco de erro), usando `<jsp:forward>`

### 3.2 Onde isso entra no projeto (padrão)

Você já tem (ou pode ter) servlets no padrão:

* `LoginServlet`
* `BuscaAlunoServlet`
* `AlunoServlet` / `AulaServlet` (CRUD)
* `UploadServlet` / `DownloadServlet`

**Ajuste para cumprir o requisito formal:**
O servlet que faz a validação principal (ex.: login, busca) deve usar:

* `request.getRequestDispatcher("/...jsp").forward(request, response);`

**Exemplo de fluxo (Login):**

1. Usuário acessa `login.jsp` (GET)
2. Envia formulário (POST) → `LoginServlet`
3. `LoginServlet` valida:

   * OK:

     * `request.setAttribute("msg", "Login realizado...");`
     * `<jsp:forward page="/WEB-INF/jsp/pages/home.jsp" />` *(na prática isso fica no servlet via RequestDispatcher)*
   * ERRO:

     * `request.setAttribute("erro", "Usuário/senha inválidos");`
     * forward para `/WEB-INF/jsp/pages/login.jsp`

**Por que `<jsp:forward>` aqui é importante:**

* mantém `request` (atributos, erros)
* evita a necessidade de querystring para mensagens
* “controle do fluxo” fica no servlet (camada de controle)

---

## 4) Upload/Download — Fluxo Completo por Usuário (2 arquivos)

### 4.1 Objetivo do requisito

* Todo usuário deve conseguir:

  1. enviar **2 arquivos**
  2. depois baixar esses arquivos **no contexto dele** (somente dele)

### 4.2 Modelo de fluxo implementável no seu cenário atual

**Camada de controle:**

* `UploadServlet`

  * GET: abre `upload.jsp` com lista de arquivos já enviados pelo usuário
  * POST: recebe multipart, valida e salva os arquivos
* `DownloadServlet`

  * GET: baixa 1 arquivo por `id` OU vários (ZIP) por seleção

**Camada de visão:**

* `upload.jsp` (reutilizando a mesma tela para upload + listagem + download)

  * Form de upload
  * Tabela com arquivos do usuário
  * Seleção por checkbox e botão “Baixar selecionados”

### 4.3 Regras críticas para “contexto do usuário”

Para garantir que o usuário só veja/baixe os próprios arquivos:

* salvar em pasta específica do usuário:

  * `/WEB-INF/uploads/<usuario>/...`
* manter na sessão um `Map<id, DocInfo>` só daquele usuário
* no download: validar `id` existe no `sessionFiles`

> Se você não quiser login para upload, você precisa de algum “identificador” para o contexto do usuário.
> Possibilidades:
>
> * sessão (mais simples, mas expira)
> * login (ideal)
> * cookie identificador (persistente) + pasta por cookie
>
> Como a regra diz “qualquer usuário no sistema”, o caminho mais correto é **via login**.
> Se você insistir em não logar, então o “usuário” vira “sessão”.

### 4.4 Validação do limite de 2 arquivos

**Onde aplicar:**

* no `UploadServlet.doPost`

**Como funciona:**

1. ler `sessionFiles`
2. contar quantos já existem
3. se `>= 2`, bloquear upload
4. se está tentando enviar mais do que cabem até completar 2, bloquear ou aceitar somente até completar (a regra do professor costuma exigir bloqueio claro)

**Mensagem esperada:**

* “Você já enviou 2 arquivos. Para enviar outros, remova algum antes.”

### 4.5 Fluxo completo (sequência)

**(A) Upload**

1. Usuário acessa `/upload` (GET)
2. `UploadServlet.doGet` faz forward para `/WEB-INF/jsp/pages/upload.jsp`

   * `request.setAttribute("sessionFiles", sessionFiles)`
3. Usuário escolhe categoria + 1 ou 2 arquivos e envia (POST `/upload`)
4. `UploadServlet.doPost`:

   * valida multipart
   * valida categoria
   * valida limite 2 arquivos (com os já existentes)
   * salva no disco (pasta do usuário)
   * atualiza `sessionFiles`
   * **forward ou redirect** para `/upload` com mensagem

**(B) Listagem e Download**

1. Usuário vê a tabela de arquivos no próprio `upload.jsp`
2. Seleciona 1 ou mais
3. Submete para `/download`
4. `DownloadServlet.doGet`:

   * se 1 arquivo: stream direto
   * se múltiplos: gera ZIP e stream
   * sempre validando que o `id` está no contexto do usuário (sessão)

---

## 5) Problema “Arquivo não existe mais no disco” e Como Ele se Relaciona ao Requisito

### 5.1 O que significa

O sistema tentou baixar um arquivo cujo caminho não existe no servidor.

### 5.2 Causa mais comum no seu caso

* Upload gravou em uma pasta e Download procurou em outra (ex.: upload em `/uploads/<user>` e download em `/uploads/public>`)
* Ou Tomcat/Eclipse está deployando em “work directory” e você está olhando outra pasta.

### 5.3 Como o requisito de “contexto do usuário” influencia isso

Se o caminho do usuário não é consistente, o download falha.
Por isso:

* o diretório base deve ser **o mesmo** para upload e download
* e, se for por usuário, a regra de path deve ser igual em ambos (`resolve(usuario)`)

---

## 6) Mapa de “Onde está cada requisito” (Checklist para sua entrega)

### 6.1 Componentes JSP Reutilizáveis

* **Header**: `/WEB-INF/jsp/components/header.jsp`
* **Footer**: `/WEB-INF/jsp/components/footer.jsp`
* **Home**: `/WEB-INF/jsp/pages/home.jsp`

  * inclui header/footer com `<jsp:include>`

**Evidência (no home.jsp):**

* `<jsp:include page="/WEB-INF/jsp/components/header.jsp" />`
* `<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />`

---

### 6.2 Fluxo Servlet com forward (sucesso/erro)

* Exemplo recomendado: `LoginServlet` (ou `BuscaAlunoServlet`)

  * em sucesso: `forward` para `home.jsp` (ou jsp de sucesso)
  * em erro: `forward` para `login.jsp` (ou jsp de erro)

**Evidência (no servlet):**

* `request.setAttribute("erro", "...");`
* `request.getRequestDispatcher("/WEB-INF/jsp/pages/login.jsp").forward(request, response);`
* `request.setAttribute("msg", "...");`
* `request.getRequestDispatcher("/WEB-INF/jsp/pages/home.jsp").forward(request, response);`

---

### 6.3 Upload de 2 arquivos por usuário + download no contexto

* `UploadServlet`

  * valida limite 2
  * salva em pasta por usuário (ou por sessão, se sem login)
  * atualiza `sessionFiles`
* `upload.jsp`

  * form upload
  * tabela com seleção de arquivos do usuário
  * botão “Baixar selecionados” (GET/POST para `/download`)
* `DownloadServlet`

  * baixa apenas arquivos presentes no `sessionFiles` do usuário

**Evidência:**

* existe `SESSION_FILES_KEY_V2 = "uploadedFilesV2"`
* `Map<String, DocInfo> sessionFiles` na sessão
* download valida `sessionFiles.get(id)` antes de abrir arquivo

---

## 7) Fluxo Completo Consolidado (Narrativa para Apresentação)

1. O usuário acessa o sistema e realiza login (ou entra e cria contexto de sessão).
2. O servlet de login processa credenciais e faz **forward**:

   * sucesso → `home.jsp`
   * erro → `login.jsp` com mensagem
3. A `home.jsp` inclui `header.jsp` e `footer.jsp` via `<jsp:include>`, garantindo reutilização de layout.
4. A partir da Home, o usuário acessa `/upload`:

   * `UploadServlet` encaminha para `upload.jsp` e injeta a lista (`sessionFiles`).
5. No `upload.jsp`, o usuário envia arquivos:

   * `UploadServlet` valida categoria e valida o limite: **máximo 2 arquivos por usuário**.
6. Após upload, o usuário visualiza os arquivos em tabela (no mesmo `upload.jsp`).
7. O usuário seleciona arquivos e baixa:

   * `DownloadServlet` valida que os `ids` pertencem ao contexto do usuário (sessão) e faz streaming (1 arquivo) ou ZIP (múltiplos).

---

## 8) Observações Finais (Coerência com o enunciado)

* O requisito de “2 arquivos por usuário” precisa de uma regra clara de identificação do usuário:

  * se há login: o contexto é o usuário logado (ideal)
  * se não há login: o contexto vira a sessão (mais frágil)
* A implementação deve garantir:

  * consistência de caminho (upload e download na mesma base)
  * bloqueio ao exceder 2 arquivos
  * listagem isolada por usuário

---

## 9) Anexo — “Como você demonstra isso na banca”

Checklist rápido do que abrir e mostrar:

1. `home.jsp` com `<jsp:include>` do header/footer
2. `header.jsp` e `footer.jsp` separados em `/components/`
3. `LoginServlet` (ou BuscaAlunoServlet) com `forward` para JSP de sucesso/erro
4. `UploadServlet` com validação de limite 2
5. `upload.jsp` com tabela de arquivos e botão baixar selecionados
6. `DownloadServlet` validando contexto do usuário antes do streaming

