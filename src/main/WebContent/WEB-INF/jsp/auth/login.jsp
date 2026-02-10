<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Login - Autoescola");
%>
<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-md-7 col-lg-6">
      <div class="card app-card">
        <div class="card-body p-4">
          <h1 class="h4 mb-3">Login (Atividade)</h1>
          <div class="text-muted mb-3">Exemplo de servlet processando requisição e roteando para JSP distintas com <code class="kbd">&lt;jsp:forward&gt;</code>.</div>

          <form method="post" action="<%= request.getContextPath() %>/login" class="needs-validation" novalidate>
            <div class="mb-3">
              <label class="form-label required">Usuário</label>
              <input name="usuario" class="form-control" required maxlength="50" placeholder="admin" />
              <div class="invalid-feedback">Informe o usuário.</div>
            </div>
            <div class="mb-3">
              <label class="form-label required">Senha</label>
              <input type="password" name="senha" class="form-control" required maxlength="50" placeholder="admin" />
              <div class="invalid-feedback">Informe a senha.</div>
            </div>

            <button class="btn btn-primary" type="submit"><i class="bi bi-box-arrow-in-right me-1"></i> Entrar</button>
          </form>

          <hr class="my-4"/>
          <div class="text-muted small">
            Padrão de teste: <code class="kbd">admin</code> / <code class="kbd">admin</code>.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
(function(){
  const forms = document.querySelectorAll('.needs-validation');
  Array.from(forms).forEach(form => {
    form.addEventListener('submit', e => {
      if (!form.checkValidity()) {
        e.preventDefault();
        e.stopPropagation();
      }
      form.classList.add('was-validated');
    }, false);
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
