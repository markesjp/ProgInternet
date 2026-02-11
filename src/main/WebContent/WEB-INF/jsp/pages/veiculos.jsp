<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<%
  request.setAttribute("pageTitle", "Veículos - Autoescola");
%>
<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<main class="container py-4">
  <div class="d-flex flex-column flex-md-row gap-2 align-items-md-center justify-content-between mb-3">
    <div>
      <h1 class="h4 mb-1">Veículos</h1>
      <div class="text-muted small">Placa em maiúsculo • Validação de duplicidade</div>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-outline-secondary" id="btnReload"><i class="bi bi-arrow-clockwise me-1"></i>Atualizar</button>
      <button class="btn btn-primary" id="btnNew"><i class="bi bi-plus-lg me-1"></i>Novo veículo</button>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-body">
      <div class="row g-2 align-items-center mb-3">
        <div class="col-12 col-md-6">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input class="form-control" id="search" placeholder="Buscar por placa, modelo, marca, status..." />
          </div>
        </div>
        <div class="col-12 col-md-6 text-md-end">
          <span class="text-muted small" id="count">0 registros</span>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light position-sticky top-0">
            <tr>
              <th style="width:70px">ID</th>
              <th>Placa</th>
              <th>Modelo</th>
              <th>Marca</th>
              <th>Ano</th>
              <th>Categoria</th>
              <th>Status</th>
              <th style="width:160px"></th>
            </tr>
          </thead>
          <tbody id="tbody">
            <tr><td colspan="8" class="text-muted">Carregando...</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</main>

<!-- Modal: Form -->
<div class="modal fade" id="modalForm" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modalTitle">Veículo</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>

      <form class="modal-body needs-validation" id="form" novalidate>
        <input type="hidden" id="id" />
        <div class="row g-3">
          <div class="col-12 col-md-4">
            <label class="form-label required">Placa</label>
            <input class="form-control" id="placa" required maxlength="8" placeholder="ABC-1234" style="text-transform:uppercase" />
            <div class="invalid-feedback">Informe a placa.</div>
          </div>
          <div class="col-12 col-md-4">
            <label class="form-label required">Ano</label>
            <input class="form-control" id="ano" type="number" required min="1900" max="2100" />
            <div class="invalid-feedback">Informe o ano.</div>
          </div>
          <div class="col-12 col-md-4">
            <label class="form-label required">Categoria</label>
            <select class="form-select" id="categoria" required>
              <option value="">Selecione...</option>
              <option value="A">A (Moto)</option>
              <option value="B">B (Carro)</option>
              <option value="C">C (Caminhão)</option>
              <option value="D">D (Ônibus)</option>
              <option value="E">E (Carreta)</option>
            </select>
            <div class="invalid-feedback">Selecione a categoria.</div>
          </div>

          <div class="col-12 col-md-6">
            <label class="form-label required">Modelo</label>
            <input class="form-control" id="modelo" required placeholder="Ex: Gol, CB 500" />
            <div class="invalid-feedback">Informe o modelo.</div>
          </div>
          <div class="col-12 col-md-6">
            <label class="form-label required">Marca</label>
            <input class="form-control" id="marca" required placeholder="Ex: VW, Honda" />
            <div class="invalid-feedback">Informe a marca.</div>
          </div>

          <div class="col-12">
            <label class="form-label required">Status</label>
            <select class="form-select" id="status" required>
              <option value="DISPONIVEL">Disponível</option>
              <option value="EM_USO">Em uso</option>
              <option value="MANUTENCAO">Manutenção</option>
            </select>
            <div class="invalid-feedback">Selecione o status.</div>
          </div>
        </div>
      </form>

      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="button" class="btn btn-primary" id="btnSave"><i class="bi bi-save me-1"></i>Salvar</button>
      </div>
    </div>
  </div>
</div>

<!-- Modal: Confirm Delete -->
<div class="modal fade" id="modalDelete" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Confirmar exclusão</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p class="mb-0">Deseja excluir o veículo <span class="fw-semibold" id="delName"></span> (ID <span id="delId"></span>)?</p>
        <div class="text-muted small mt-2">Essa ação não pode ser desfeita.</div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="button" class="btn btn-danger" id="btnConfirmDelete"><i class="bi bi-trash me-1"></i>Excluir</button>
      </div>
    </div>
  </div>
</div>

<!-- Toast -->
<div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1080">
  <div id="toast" class="toast align-items-center" role="alert" aria-live="assertive" aria-atomic="true">
    <div class="d-flex">
      <div class="toast-body" id="toastMsg"></div>
      <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
  const BASE = '<%= request.getContextPath() %>';
  const API_VEICULOS = BASE + '/veiculos';

  const tbody = document.getElementById('tbody');
  const search = document.getElementById('search');
  const count = document.getElementById('count');

  const modalForm = new bootstrap.Modal('#modalForm');
  const modalDelete = new bootstrap.Modal('#modalDelete');

  const toastEl = document.getElementById('toast');
  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  const toastMsg = document.getElementById('toastMsg');

  const form = document.getElementById('form');
  const fields = {
    id: document.getElementById('id'),
    placa: document.getElementById('placa'),
    modelo: document.getElementById('modelo'),
    marca: document.getElementById('marca'),
    ano: document.getElementById('ano'),
    categoria: document.getElementById('categoria'),
    status: document.getElementById('status'),
  };

  let rows = [];
  let deleteTarget = null;

  function showToast(message, isError = false) {
    toastEl.classList.remove('text-bg-danger', 'text-bg-success');
    toastEl.classList.add(isError ? 'text-bg-danger' : 'text-bg-success');
    toastMsg.textContent = message;
    toast.show();
  }

  function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, (c) => ({
      '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
    }[c]));
  }

  fields.placa.addEventListener('input', (e) => {
    e.target.value = String(e.target.value || '').toUpperCase();
  });

  function normalizeList(resp) {
    if (Array.isArray(resp)) return resp;
    if (Array.isArray(resp?.data)) return resp.data;
    return [];
  }
  function normalizeItem(resp) {
    return resp?.data ?? resp;
  }

  async function apiGet(op, params = {}) {
    const url = new URL(API_VEICULOS, window.location.origin);
    url.searchParams.set('format', 'json');
    url.searchParams.set('op', op);
    for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);

    const res = await fetch(url.toString(), { headers: { 'Accept': 'application/json' } });
    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
    return data;
  }

  async function apiPost(op, body) {
    const res = await fetch(API_VEICULOS + '?format=json', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 'Accept': 'application/json' },
      body: new URLSearchParams({ op, ...body }).toString()
    });
    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
    return data;
  }

  function renderTable(list) {
    tbody.innerHTML = '';
    if (!list.length) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-muted">Nenhum registro.</td></tr>';
      count.textContent = '0 registros';
      return;
    }

    count.textContent = `${list.length} registro(s)`;
    for (const v of list) {
      const st = String(v.status || '').toUpperCase();
      const badge = st === 'DISPONIVEL' ? 'text-bg-success' : (st === 'MANUTENCAO' ? 'text-bg-warning' : 'text-bg-secondary');

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="text-muted">${escapeHtml(v.id)}</td>
        <td class="fw-semibold">${escapeHtml(String(v.placa || '').toUpperCase())}</td>
        <td>${escapeHtml(v.modelo || '')}</td>
        <td>${escapeHtml(v.marca || '')}</td>
        <td>${escapeHtml(v.ano || '')}</td>
        <td><span class="badge text-bg-light border">${escapeHtml(v.categoria || '')}</span></td>
        <td><span class="badge ${badge}">${escapeHtml(st)}</span></td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${escapeHtml(v.id)}"><i class="bi bi-pencil-square"></i></button>
          <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${escapeHtml(v.id)}"><i class="bi bi-trash"></i></button>
        </td>
      `;
      tbody.appendChild(tr);
    }
  }

  function applyFilter() {
    const q = (search.value || '').toLowerCase();
    if (!q) return renderTable(rows);

    const filtered = rows.filter(r =>
      String(r.placa || '').toLowerCase().includes(q) ||
      String(r.modelo || '').toLowerCase().includes(q) ||
      String(r.marca || '').toLowerCase().includes(q) ||
      String(r.status || '').toLowerCase().includes(q) ||
      String(r.categoria || '').toLowerCase().includes(q)
    );
    renderTable(filtered);
  }

  async function load() {
    tbody.innerHTML = '<tr><td colspan="8" class="text-muted">Carregando...</td></tr>';
    try {
      const resp = await apiGet('list');
      rows = normalizeList(resp);
      applyFilter();
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8" class="text-danger">${escapeHtml(e.message)}</td></tr>`;
    }
  }

  function openNew() {
    document.getElementById('modalTitle').textContent = 'Novo veículo';
    fields.id.value = '';
    for (const k of Object.keys(fields)) if (k !== 'id') fields[k].value = '';
    fields.status.value = 'DISPONIVEL';
    form.classList.remove('was-validated');
    modalForm.show();
    setTimeout(() => fields.placa.focus(), 150);
  }

  function openEdit(v) {
    document.getElementById('modalTitle').textContent = `Editar veículo #${v.id}`;
    fields.id.value = v.id;
    fields.placa.value = String(v.placa || '').toUpperCase();
    fields.modelo.value = v.modelo || '';
    fields.marca.value = v.marca || '';
    fields.ano.value = v.ano || '';
    fields.categoria.value = v.categoria || '';
    fields.status.value = String(v.status || 'DISPONIVEL').toUpperCase();
    form.classList.remove('was-validated');
    modalForm.show();
  }

  async function save() {
    form.classList.add('was-validated');
    if (!form.checkValidity()) return;

    const payload = {
      id: fields.id.value,
      placa: String(fields.placa.value || '').toUpperCase().trim(),
      modelo: fields.modelo.value,
      marca: fields.marca.value,
      ano: fields.ano.value,
      categoria: fields.categoria.value,
      status: fields.status.value,
    };

    try {
      const isUpdate = !!payload.id;
      const res = await apiPost(isUpdate ? 'update' : 'create', payload);
      modalForm.hide();
      showToast(res?.message || 'Salvo com sucesso.');
      await load();
    } catch (e) {
      showToast(e.message, true);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) return;
    try {
      const res = await apiPost('delete', { id: deleteTarget.id });
      modalDelete.hide();
      showToast(res?.message || 'Excluído com sucesso.');
      await load();
    } catch (e) {
      showToast(e.message, true);
    } finally {
      deleteTarget = null;
    }
  }

  document.getElementById('btnNew').addEventListener('click', openNew);
  document.getElementById('btnReload').addEventListener('click', load);
  document.getElementById('btnSave').addEventListener('click', save);
  document.getElementById('btnConfirmDelete').addEventListener('click', confirmDelete);
  search.addEventListener('input', applyFilter);

  tbody.addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;

    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    const row = rows.find(r => String(r.id) === String(id));
    if (!row) return;

    if (action === 'edit') {
      try {
        const detailResp = await apiGet('get', { id });
        openEdit(normalizeItem(detailResp));
      } catch (err) {
        showToast(err.message, true);
      }
      return;
    }

    if (action === 'delete') {
      deleteTarget = row;
      document.getElementById('delName').textContent = row.placa;
      document.getElementById('delId').textContent = row.id;
      modalDelete.show();
    }
  });

  load();
</script>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
