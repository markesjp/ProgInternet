<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Upload Concluído - Autoescola");
  String a1 = (String) request.getAttribute("arquivo1Nome");
  String a2 = (String) request.getAttribute("arquivo2Nome");
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<div class="container py-5">
  <div class="card app-card">
    <div class="card-body p-4">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
        <h1 class="h4 m-0">Upload realizado</h1>
        <div class="d-flex gap-2 flex-wrap">
          <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/upload">Novo upload</a>
          <a class="btn btn-dark" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
      </div>

      <div class="alert alert-success">
        Os dois arquivos foram enviados com sucesso.
      </div>

      <div class="row g-3">
        <div class="col-12 col-lg-6">
          <div class="p-3 border rounded bg-light">
            <div class="fw-semibold">Arquivo 1</div>
            <div class="text-muted"><%= a1 == null ? "(não informado)" : a1 %></div>
            <div class="mt-2">
              <a class="btn btn-sm btn-outline-primary" href="<%= request.getContextPath() %>/download?slot=1">
                <i class="bi bi-download me-1"></i>Baixar
              </a>
            </div>
          </div>
        </div>

        <div class="col-12 col-lg-6">
          <div class="p-3 border rounded bg-light">
            <div class="fw-semibold">Arquivo 2</div>
            <div class="text-muted"><%= a2 == null ? "(não informado)" : a2 %></div>
            <div class="mt-2">
              <a class="btn btn-sm btn-outline-primary" href="<%= request.getContextPath() %>/download?slot=2">
                <i class="bi bi-download me-1"></i>Baixar
              </a>
            </div>
          </div>
        </div>
      </div>

      <hr class="my-4"/>

      <div class="text-muted small">
        Observação: o download é feito pelo <code>DownloadServlet</code> usando os caminhos armazenados na sessão,
        garantindo que cada usuário baixe apenas os próprios arquivos.
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
