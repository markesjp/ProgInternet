<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<%@ page import="java.util.*" %>
<%@ page import="autoescola.controller.UploadServlet" %>
<%
    String msg = request.getParameter("msg");
    String erro = request.getParameter("erro");

    @SuppressWarnings("unchecked")
    Map<String, UploadServlet.DocInfo> sessionFiles =
            (Map<String, UploadServlet.DocInfo>) request.getAttribute("sessionFiles");
    if (sessionFiles == null) sessionFiles = new LinkedHashMap<>();
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Upload de Comprovantes</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">Upload de comprovantes</h3>
    <a class="btn btn-outline-secondary" href="<%=request.getContextPath()%>/home">Voltar</a>
  </div>

  <% if (msg != null && !msg.isBlank()) { %>
    <div class="alert alert-success"><%= msg %></div>
  <% } %>

  <% if (erro != null && !erro.isBlank()) { %>
    <div class="alert alert-danger"><%= erro %></div>
  <% } %>

  <!-- CARD UPLOAD -->
  <div class="card shadow-sm mb-3">
    <div class="card-body">
      <form action="<%=request.getContextPath()%>/upload" method="post" enctype="multipart/form-data">
        <div class="mb-3">
          <label class="form-label">Categoria do comprovante</label>
          <select class="form-select" name="categoria" required>
            <option value="">Selecione...</option>
            <option value="PIX">PIX</option>
            <option value="BOLETO">Boleto</option>
            <option value="TED">TED</option>
            <option value="DOC">DOC</option>
            <option value="OUTROS">Outros</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Arquivos</label>
          <input class="form-control" type="file" name="arquivos" multiple required />
          <div class="form-text">Você pode selecionar múltiplos arquivos.</div>
        </div>

        <button class="btn btn-primary" type="submit">Enviar</button>
      </form>
    </div>
  </div>

  <!-- CARD LISTAGEM + DOWNLOAD -->
  <div class="card shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5 class="mb-0">Arquivos enviados (nesta sessão)</h5>

        <div class="d-flex gap-2">
          <button form="formDownload" class="btn btn-sm btn-outline-primary" type="submit">
            Baixar selecionados (ZIP)
          </button>
        </div>
      </div>

      <% if (sessionFiles.isEmpty()) { %>
        <div class="text-muted">Nenhum arquivo enviado ainda.</div>
      <% } else { %>

      <form id="formDownload" action="<%=request.getContextPath()%>/download" method="get">
        <div class="table-responsive">
          <table class="table table-sm align-middle">
            <thead>
              <tr>
                <th style="width:40px;">
                  <input id="chkAll" class="form-check-input" type="checkbox" />
                </th>
                <th>Nome</th>
                <th>Categoria</th>
                <th>Tamanho</th>
                <th>Tipo</th>
                <th style="width:120px;"></th>
              </tr>
            </thead>
            <tbody>
              <%
                for (UploadServlet.DocInfo f : sessionFiles.values()) {
                    String size =
                        (f.sizeBytes >= 1024*1024)
                        ? String.format(Locale.US, "%.2f MB", (f.sizeBytes / 1024.0 / 1024.0))
                        : String.format(Locale.US, "%.1f KB", (f.sizeBytes / 1024.0));
              %>
              <tr>
                <td>
                  <input class="form-check-input chkItem" type="checkbox" name="ids" value="<%= f.id %>" />
                </td>
                <td><%= f.originalName %></td>
                <td><%= f.categoria %></td>
                <td><%= size %></td>
                <td><%= f.contentType %></td>
                <td class="text-end">
                  <a class="btn btn-sm btn-outline-secondary"
                     href="<%=request.getContextPath()%>/download?id=<%= f.id %>">
                    Baixar
                  </a>
                </td>
              </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </form>

      <div class="text-muted small">
        • Se selecionar 1 arquivo e clicar “Baixar selecionados”, ele baixa direto (sem zip).<br/>
        • Se selecionar 2+ arquivos, baixa um ZIP com todos.
      </div>

      <% } %>
    </div>
  </div>
</div>

<script>
  (function(){
    const chkAll = document.getElementById('chkAll');
    const items = () => Array.from(document.querySelectorAll('.chkItem'));

    if (chkAll) {
      chkAll.addEventListener('change', function(){
        items().forEach(chk => chk.checked = chkAll.checked);
      });
    }

    document.addEventListener('change', function(e){
      if (!e.target.classList.contains('chkItem')) return;
      const all = items();
      const checked = all.filter(x => x.checked).length;
      chkAll.checked = (checked === all.length);
      chkAll.indeterminate = (checked > 0 && checked < all.length);
    });
  })();
</script>

</body>
</html>
