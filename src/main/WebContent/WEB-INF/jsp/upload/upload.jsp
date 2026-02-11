<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Upload de Arquivos - Autoescola");
  String usuarioLogado = (String) session.getAttribute("usuarioLogado");
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="card app-card">
    <div class="card-body p-4">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
        <h1 class="h4 m-0">Upload de 2 arquivos</h1>
        <div class="d-flex gap-2 flex-wrap">
          <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
      </div>

      <% if (usuarioLogado == null) { %>
        <div class="alert alert-warning">
          Você ainda não está logado. Para atender ao requisito de "arquivos por usuário", faça login antes.
          <a class="alert-link" href="<%= request.getContextPath() %>/login">Ir para Login</a>
        </div>
      <% } else { %>
        <div class="alert alert-info">
          Usuário logado: <strong><%= usuarioLogado %></strong>
        </div>
      <% } %>

      <form method="post" action="<%= request.getContextPath() %>/upload" enctype="multipart/form-data" class="row g-3">
        <div class="col-12 col-lg-6">
          <label class="form-label">Arquivo 1 (obrigatório)</label>
          <input class="form-control" type="file" name="arquivo1" required />
        </div>
        <div class="col-12 col-lg-6">
          <label class="form-label">Arquivo 2 (obrigatório)</label>
          <input class="form-control" type="file" name="arquivo2" required />
        </div>

        <div class="col-12">
          <div class="d-flex gap-2 flex-wrap">
            <button class="btn btn-primary" type="submit"><i class="bi bi-upload me-1"></i>Enviar</button>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/upload">Limpar</a>
          </div>
        </div>

        <div class="col-12">
          <div class="text-muted small">
            Observação: o servlet de upload usa <code>commons-fileupload2</code> + <code>commons-io</code>.
            Basta colocar os JARs em <code>WEB-INF/lib</code>.
          </div>
        </div>
      </form>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
