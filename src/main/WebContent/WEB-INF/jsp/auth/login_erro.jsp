<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Erro no Login - Autoescola");
  String erro = (String) request.getAttribute("erro");
  if (erro == null || erro.isBlank()) erro = "Usuário ou senha inválidos.";
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-md-7 col-lg-6">
      <div class="card app-card">
        <div class="card-body p-4">
          <div class="alert alert-danger">
            <strong>Falha no login.</strong><br/>
            <%= erro %>
          </div>

          <div class="d-flex gap-2 flex-wrap">
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/login">Tentar novamente</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Voltar</a>
          </div>

          <hr class="my-4"/>
          <div class="text-muted small">
            Dica: padrão de teste <code class="kbd">admin/admin</code>.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
