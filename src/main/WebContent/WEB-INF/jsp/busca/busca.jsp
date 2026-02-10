<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Buscar Aluno - Autoescola");
  String msg = (String) request.getAttribute("msg");
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-lg-8">
      <div class="card app-card">
        <div class="card-body p-4">
          <h1 class="h4 mb-3">Buscar Aluno</h1>

          <% if (msg != null && !msg.isBlank()) { %>
            <div class="alert alert-warning"><%= msg %></div>
          <% } %>

          <form method="post" action="<%= request.getContextPath() %>/buscar-aluno" class="row g-3" novalidate>
            <div class="col-12 col-md-6">
              <label class="form-label">CPF (opcional)</label>
              <input id="cpfBusca" name="cpf" class="form-control" placeholder="000.000.000-00" maxlength="14" inputmode="numeric">
              <div class="form-text">Você pode informar CPF, Nome ou ambos.</div>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label">Nome (opcional)</label>
              <input name="nome" class="form-control" placeholder="Ex.: Maria Silva" maxlength="120">
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label">Status (opcional)</label>
              <select name="status" class="form-select">
                <option value="">Todos</option>
                <option value="ATIVO">ATIVO</option>
                <option value="INATIVO">INATIVO</option>
              </select>
            </div>

            <div class="col-12 d-flex gap-2 flex-wrap mt-2">
              <button class="btn btn-primary" type="submit">Buscar</button>
              <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Voltar</a>
            </div>
          </form>

          <hr class="my-4"/>

          <div class="text-muted small">
            A busca é processada pelo <strong>Servlet</strong>, que encaminha para JSP de sucesso/erro usando <code class="kbd">&lt;jsp:forward&gt;</code>.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
  // Máscara simples de CPF (sem bibliotecas)
  (function(){
    const cpf = document.getElementById('cpfBusca');
    if (!cpf) return;
    cpf.addEventListener('input', () => {
      let v = cpf.value.replace(/\D/g,'').slice(0,11);
      v = v.replace(/(\d{3})(\d)/,'$1.$2');
      v = v.replace(/(\d{3})(\d)/,'$1.$2');
      v = v.replace(/(\d{3})(\d{1,2})$/,'$1-$2');
      cpf.value = v;
    });
  })();
</script>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
