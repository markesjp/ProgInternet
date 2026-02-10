<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Busca - Erro - Autoescola");
  String erro = (String) request.getAttribute("erro");
  if (erro == null || erro.isBlank()) erro = "Nenhum resultado encontrado ou dados inválidos.";
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-lg-8">
      <div class="card app-card">
        <div class="card-body p-4">
          <h1 class="h4 mb-3">Busca não concluída</h1>

          <div class="alert alert-danger">
            <strong>Não foi possível concluir a busca.</strong><br/>
            <%= erro %>
          </div>

          <div class="d-flex gap-2 flex-wrap">
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/buscar-aluno">Voltar para busca</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Home</a>
          </div>

          <hr class="my-4"/>
          <div class="text-muted small">
            Se você informou CPF, confira se está completo (11 dígitos). Você também pode buscar apenas pelo nome.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
