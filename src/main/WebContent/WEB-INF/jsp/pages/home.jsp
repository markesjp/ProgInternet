<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<%
  request.setAttribute("pageTitle", "Home - Autoescola");
%>
<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="row g-3">
    <div class="col-12">
      <div class="card app-card">
        <div class="card-body p-4">
          <h1 class="h4 mb-1">Autoescola • Sistema</h1>
          <div class="text-muted">CRUD com Servlets + JSP (includes) + MySQL, com regras profissionais (status/histórico).</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
      <a class="text-decoration-none" href="<%= request.getContextPath() %>/alunos">
        <div class="card app-card h-100">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-people fs-3"></i>
              <div>
                <div class="fw-semibold">Alunos</div>
                <div class="text-muted small">Ativo/Inativo + desmarcação automática</div>
              </div>
            </div>
          </div>
        </div>
      </a>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
      <a class="text-decoration-none" href="<%= request.getContextPath() %>/aulas">
        <div class="card app-card h-100">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-calendar-check fs-3"></i>
              <div>
                <div class="fw-semibold">Aulas</div>
                <div class="text-muted small">Marcada/Desmarcada/Concluída + conflitos</div>
              </div>
            </div>
          </div>
        </div>
      </a>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
      <a class="text-decoration-none" href="<%= request.getContextPath() %>/instrutores">
        <div class="card app-card h-100">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-person-badge fs-3"></i>
              <div>
                <div class="fw-semibold">Instrutores</div>
                <div class="text-muted small">Validações + bloqueio por aulas vinculadas</div>
              </div>
            </div>
          </div>
        </div>
      </a>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
      <a class="text-decoration-none" href="<%= request.getContextPath() %>/veiculos">
        <div class="card app-card h-100">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-truck-front fs-3"></i>
              <div>
                <div class="fw-semibold">Veículos</div>
                <div class="text-muted small">Placa com máscara + bloqueio por aulas vinculadas</div>
              </div>
            </div>
          </div>
        </div>
      </a>
    </div>

    <div class="col-12">
      <div class="card app-card">
        <div class="card-body p-4">
          <div class="d-flex flex-wrap gap-2">
            <a class="btn btn-outline-dark" href="<%= request.getContextPath() %>/buscar-aluno"><i class="bi bi-search me-1"></i> Busca de Aluno</a>
           
            <a class="btn btn-outline-success" href="<%= request.getContextPath() %>/upload"><i class="bi bi-upload me-1"></i> Uploads (atividade)</a>
          </div>
          <div class="text-muted small mt-2">
            Dica: para a migração do banco (status/histórico), rode o script em <code>WEB-INF/sql/autoescola.sql</code>.
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
