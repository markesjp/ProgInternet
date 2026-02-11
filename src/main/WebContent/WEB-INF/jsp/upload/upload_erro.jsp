<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Erro no Upload - Autoescola");
  String erro = (String) request.getAttribute("erro");
  if (erro == null || erro.isBlank()) erro = "Não foi possível realizar o upload.";
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="card app-card">
    <div class="card-body p-4">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
        <h1 class="h4 m-0">Erro no Upload</h1>
        <div class="d-flex gap-2 flex-wrap">
          <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/upload">Tentar novamente</a>
          <a class="btn btn-dark" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
      </div>

      <div class="alert alert-danger">
        <%= erro %>
      </div>

      <div class="text-muted small">
        Dica: confirme se você adicionou os JARs do <code>commons-fileupload2</code> e <code>commons-io</code> em <code>WEB-INF/lib</code>.
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
