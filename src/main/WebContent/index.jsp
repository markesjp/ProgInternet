<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<%
  request.setAttribute("pageTitle", "Home - Autoescola");

  // Info dos arquivos já enviados (seu controller pode preencher isso; caso não preencha, mostramos "não enviado")
  autoescola.util.DocumentosStore.DocInfo doc1 = (autoescola.util.DocumentosStore.DocInfo) request.getAttribute("doc1");
  autoescola.util.DocumentosStore.DocInfo doc2 = (autoescola.util.DocumentosStore.DocInfo) request.getAttribute("doc2");

  String msg = request.getParameter("msg");
  String erro = request.getParameter("erro");
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp"/>

<body class="bg-light">
  <jsp:include page="/WEB-INF/jsp/components/menu.jsp"/>

  <main class="container py-4">
    <div class="p-4 bg-white rounded shadow-sm">
      <h1 class="h4 mb-1">Bem-vindo(a)!</h1>
      <p class="text-muted mb-0">Use o menu para acessar os cadastros e listagens.</p>
    </div>

    <div class="row g-3 mt-3">
      <!-- Ações rápidas -->
      <div class="col-md-6">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6">Ações rápidas</h2>
            <div class="d-grid gap-2">
              <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/alunos">Gerenciar Alunos</a>
              <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/instrutores">Gerenciar Instrutores</a>
              <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/veiculos">Gerenciar Veículos</a>
              <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/aulas">Gerenciar Aulas</a>
            </div>
          </div>
        </div>
      </div>

      <!-- Upload/Download de comprovantes -->
      <div class="col-md-6">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <h2 class="h6 mb-0">Comprovantes (Upload/Download)</h2>
              <span class="badge text-bg-light border">Fotos • PDF • TXT</span>
            </div>

            <p class="text-muted small mb-3">
              Envie comprovantes de pagamentos, notas de gasolina e arquivos de texto.
              Os arquivos ficam associados ao usuário/sessão (conforme sua implementação).
            </p>

            <% if (msg != null && !msg.isBlank()) { %>
              <div class="alert alert-success py-2"><%= msg %></div>
            <% } %>
            <% if (erro != null && !erro.isBlank()) { %>
              <div class="alert alert-danger py-2"><%= erro %></div>
            <% } %>

            <form action="<%= request.getContextPath() %>/upload" method="post" enctype="multipart/form-data" class="needs-validation" novalidate>
              <div class="mb-3">
                <label class="form-label fw-semibold">Arquivo 1 (obrigatório)</label>
                <input class="form-control" type="file" name="arquivo1"
                       accept="image/*,.pdf,.txt,application/pdf,text/plain" required />
                <div class="form-text">
                  Status:
                  <% if (doc1 == null) { %>
                    <span class="text-muted">não enviado</span>
                  <% } else { %>
                    <span class="fw-semibold"><%= doc1.originalName %></span>
                    (<%= doc1.sizeBytes %> bytes)
                    - <a href="<%= request.getContextPath() %>/download?slot=1">Download</a>
                  <% } %>
                </div>
                <div class="invalid-feedback">Selecione o Arquivo 1.</div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-semibold">Arquivo 2 (obrigatório)</label>
                <input class="form-control" type="file" name="arquivo2"
                       accept="image/*,.pdf,.txt,application/pdf,text/plain" required />
                <div class="form-text">
                  Status:
                  <% if (doc2 == null) { %>
                    <span class="text-muted">não enviado</span>
                  <% } else { %>
                    <span class="fw-semibold"><%= doc2.originalName %></span>
                    (<%= doc2.sizeBytes %> bytes)
                    - <a href="<%= request.getContextPath() %>/download?slot=2">Download</a>
                  <% } %>
                </div>
                <div class="invalid-feedback">Selecione o Arquivo 2.</div>
              </div>

              <div class="d-flex gap-2">
                <button class="btn btn-primary" type="submit">
                  <i class="bi bi-cloud-arrow-up me-1"></i>Enviar/Atualizar
                </button>
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/upload">
                  <i class="bi bi-folder2-open me-1"></i>Abrir tela de upload
                </a>
              </div>

              <div class="form-text mt-2">
                Dica: use PDF ou foto (JPG/PNG) para comprovantes. TXT serve para observações ou logs.
              </div>
            </form>

            <hr class="my-3">

            <div class="text-muted small">
              Segurança: o download só funciona para usuário logado e com proteção contra path traversal.
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>

  <jsp:include page="/WEB-INF/jsp/components/footer.jsp"/>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    // validação bootstrap
    (function () {
      const forms = document.querySelectorAll('.needs-validation');
      Array.from(forms).forEach((form) => {
        form.addEventListener('submit', (event) => {
          if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
          }
          form.classList.add('was-validated');
        }, false);
      });
    })();
  </script>
</body>
</html>
