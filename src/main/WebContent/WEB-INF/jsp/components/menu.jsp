<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String ctx = request.getContextPath();
%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand fw-semibold" href="<%= ctx %>/home">Autoescola</a>

    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
      <span class="navbar-toggler-icon"></span>
    </button>

    <div id="navMain" class="collapse navbar-collapse">
      <ul class="navbar-nav ms-auto gap-lg-1">
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/home">Home</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/alunos">Alunos</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/instrutores">Instrutores</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/veiculos">Veículos</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/aulas">Aulas</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/buscar-aluno">Busca</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/login">Login</a></li>
      </ul>
    </div>
  </div>
</nav>
