<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*, autoescola.model.Aluno" %>
<%
  request.setAttribute("pageTitle", "Resultado da Busca - Autoescola");

  @SuppressWarnings("unchecked")
  List<Aluno> alunos = (List<Aluno>) request.getAttribute("alunos");
  if (alunos == null) alunos = new ArrayList<>();
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="card app-card">
    <div class="card-body p-4">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
        <h1 class="h4 m-0">Resultado da Busca</h1>
        <div class="d-flex gap-2 flex-wrap">
          <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/buscar-aluno">Nova busca</a>
          <a class="btn btn-dark" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
      </div>

      <div class="alert alert-success">
        Encontrado(s) <strong><%= alunos.size() %></strong> aluno(s).
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light">
            <tr>
              <th style="width: 90px;">ID</th>
              <th>Nome</th>
              <th style="width: 180px;">CPF</th>
              <th style="width: 120px;">Status</th>
            </tr>
          </thead>
          <tbody>
          <% for (Aluno a : alunos) { 
               String status = a.getStatus();
               if (status == null || status.isBlank()) status = "ATIVO";
               String badge = "ATIVO".equalsIgnoreCase(status) ? "bg-success" : "bg-secondary";
          %>
            <tr>
              <td><%= a.getId() %></td>
              <td><%= a.getNome() %></td>
              <td><%= a.getCpf() %></td>
              <td><span class="badge <%= badge %>"><%= status %></span></td>
            </tr>
          <% } %>
          </tbody>
        </table>
      </div>

      <hr class="my-4"/>

      <div class="text-muted small">
        Dica: você pode abrir o CRUD de alunos e pesquisar pelo ID.
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
