<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/home">
      <i class="bi bi-car-front-fill me-2"></i>Autoescola
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
      <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navMain">
      <ul class="navbar-nav me-auto">
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/home">Home</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/alunos">Alunos</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/instrutores">Instrutores</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/veiculos">Veículos</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/aulas">Aulas</a></li>
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/busca-aluno">Busca (JSP forward)</a></li>
      </ul>
      <ul class="navbar-nav ms-auto">
        <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/login_form">Login</a></li>
      </ul>
    </div>
  </div>
</nav>
