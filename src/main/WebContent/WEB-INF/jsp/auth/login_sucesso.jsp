<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Login OK - Autoescola");
  String usuario = (String) request.getAttribute("usuario");
  if (usuario == null) usuario = "usuário";
%>
<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-md-8">
      <div class="card app-card">
        <div class="card-body p-4">
          <div class="alert alert-success mb-3">
            <strong>Login realizado com sucesso.</strong><br/>
            Bem-vindo(a), <%= usuario %>.
          </div>

          <div class="d-flex gap-2 flex-wrap">
            <a class="btn btn-dark" href="<%= request.getContextPath() %>/home">Ir para Home</a>
            <a class="btn btn-outline-success" href="<%= request.getContextPath() %>/upload"><i class="bi bi-upload me-1"></i>Uploads</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/login">Voltar</a>
          </div>

          <hr class="my-4"/>
          <div class="text-muted small">
            Neste exemplo, o Servlet processa o login e encaminha para uma JSP "roteadora" que faz <code class="kbd">&lt;jsp:forward&gt;</code> para sucesso/erro.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
