<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Instrutores - Autoescola");
%>
<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<main class="container py-4">
    <div class="d-flex flex-column flex-md-row gap-2 align-items-md-center justify-content-between mb-3">
      <div>
        <h1 class="h4 mb-1">Instrutores</h1>
        <div class="text-muted small">Listagem com ações rápidas de edição/exclusão.</div>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" id="btnReload"><i class="bi bi-arrow-clockwise me-1"></i>Atualizar</button>
        <button class="btn btn-success" id="btnNew"><i class="bi bi-plus-lg me-1"></i>Novo instrutor</button>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div class="row g-2 align-items-center mb-3">
          <div class="col-12 col-md-6">
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-search"></i></span>
              <input class="form-control" id="search" placeholder="Buscar por nome, CPF ou especialidade..." />
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
                <th>Nome</th>
                <th>CPF</th>
                <th>Especialidade</th>
                <th>Contratação</th>
                <th style="width:160px"></th>
              </tr>
            </thead>
            <tbody id="tbody">
              <tr><td colspan="6" class="text-muted">Carregando...</td></tr>
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
          <h5 class="modal-title" id="modalTitle">Instrutor</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <form class="modal-body" id="form">
          <input type="hidden" id="id" />
          <div class="row g-3">
            <div class="col-12 col-md-8">
              <label class="form-label required">Nome</label>
              <input class="form-control" id="nome" required minlength="2" />
              <div class="invalid-feedback">Informe o nome (mín. 2 caracteres).</div>
            </div>
            <div class="col-12 col-md-4">
              <label class="form-label required">CPF</label>
              <input class="form-control" id="cpf" required maxlength="14" placeholder="000.000.000-00" />
              <div class="invalid-feedback">Informe o CPF.</div>
            </div>

            <div class="col-12 col-md-4">
              <label class="form-label">Telefone</label>
              <input class="form-control" id="telefone" placeholder="(00) 00000-0000" />
            </div>
            <div class="col-12 col-md-4">
              <label class="form-label required">Especialidade</label>
              <select class="form-select" id="especialidade" required>
                <option value="">Selecione...</option>
                <option value="A">Categoria A</option>
                <option value="B">Categoria B</option>
                <option value="AB">Categoria AB</option>
                <option value="C">Categoria C</option>
                <option value="D">Categoria D</option>
                <option value="E">Categoria E</option>
              </select>
              <div class="invalid-feedback">Selecione a especialidade.</div>
            </div>
            <div class="col-12 col-md-4">
              <label class="form-label required">Data de contratação</label>
              <input class="form-control" id="data_contratacao" type="date" required />
              <div class="invalid-feedback">Informe a data de contratação.</div>
            </div>
          </div>
        </form>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
          <button type="button" class="btn btn-success" id="btnSave"><i class="bi bi-save me-1"></i>Salvar</button>
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
          <p class="mb-0">Deseja excluir o instrutor <span class="fw-semibold" id="delName"></span> (ID <span id="delId"></span>)?</p>
          <div class="text-muted small mt-2">Essa ação não pode ser desfeita.</div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
          <button type="button" class="btn btn-danger" id="btnConfirmDelete"><i class="bi bi-trash me-1"></i>Excluir</button>
        </div>
      </div>
    </div>
  </div>

  <!-- Toasts -->
  <div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1080">
    <div id="toast" class="toast align-items-center" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="d-flex">
        <div class="toast-body" id="toastMsg"></div>
        <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
    </div>
  </div>

  
  <script>
    const API = 'instrutores';
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
      nome: document.getElementById('nome'),
      cpf: document.getElementById('cpf'),
      telefone: document.getElementById('telefone'),
      especialidade: document.getElementById('especialidade'),
      data_contratacao: document.getElementById('data_contratacao'),
    };

    let rows = [];
    let deleteTarget = null;

    function showToast(message, isError = false) {
      toastEl.classList.remove('text-bg-danger', 'text-bg-success');
      toastEl.classList.add(isError ? 'text-bg-danger' : 'text-bg-success');
      toastMsg.textContent = message;
      toast.show();
    }

    function qsParam(name) {
      return new URLSearchParams(window.location.search).get(name);
    }

    function applyMaskCPF(value) {
      const digits = value.replace(/\D/g, '').slice(0, 11);
      const parts = [];
      parts.push(digits.slice(0, 3));
      if (digits.length >= 4) parts.push(digits.slice(3, 6));
      if (digits.length >= 7) parts.push(digits.slice(6, 9));
      let out = parts.filter(Boolean).join('.');
      if (digits.length >= 10) out += '-' + digits.slice(9, 11);
      return out;
    }

    fields.cpf.addEventListener('input', (e) => {
      const cur = e.target.value;
      e.target.value = applyMaskCPF(cur);
    });

    async function apiGet(op, params = {}) {
      const url = new URL(API, window.location.href);
      url.searchParams.set('format', 'json');
      url.searchParams.set('op', op);
      for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
      const res = await fetch(url.toString(), { headers: { 'Accept': 'application/json' } });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
      return data;
    }

    async function apiPost(op, body) {
      const res = await fetch(API + '?format=json', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 'Accept': 'application/json' },
        body: new URLSearchParams({ op, ...body }).toString()
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
      return data;
    }

    function escapeHtml(s) {
      return String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
    }

    function renderTable(list) {
      tbody.innerHTML = '';
      if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Nenhum registro.</td></tr>';
        count.textContent = '0 registros';
        return;
      }
      count.textContent = `${list.length} registro(s)`;
      for (const i of list) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td class="text-muted">${i.id}</td>
          <td>
            <div class="fw-semibold">${escapeHtml(i.nome)}</div>
            <div class="text-muted small">${escapeHtml(i.telefone || '')}</div>
          </td>
          <td>${escapeHtml(i.cpf)}</td>
          <td><span class="badge text-bg-light border">${escapeHtml(i.especialidade)}</span></td>
          <td>${escapeHtml(i.data_contratacao || '')}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${i.id}"><i class="bi bi-pencil-square"></i></button>
            <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${i.id}"><i class="bi bi-trash"></i></button>
          </td>
        `;
        tbody.appendChild(tr);
      }
    }

    function openNew() {
      document.getElementById('modalTitle').textContent = 'Novo instrutor';
      fields.id.value = '';
      for (const k of Object.keys(fields)) if (k !== 'id') fields[k].value = '';
      form.classList.remove('was-validated');
      modalForm.show();
      setTimeout(() => fields.nome.focus(), 250);
    }

    function openEdit(i) {
      document.getElementById('modalTitle').textContent = `Editar instrutor #${i.id}`;
      fields.id.value = i.id;
      fields.nome.value = i.nome || '';
      fields.cpf.value = i.cpf || '';
      fields.telefone.value = i.telefone || '';
      fields.especialidade.value = i.especialidade || '';
      fields.data_contratacao.value = i.data_contratacao || '';
      form.classList.remove('was-validated');
      modalForm.show();
    }

    async function load() {
      tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Carregando...</td></tr>';
      try {
        rows = await apiGet('list');
        applyFilter();
      } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(e.message)}</td></tr>`;
      }
    }

    function applyFilter() {
      const q = (search.value || '').toLowerCase();
      if (!q) return renderTable(rows);
      const filtered = rows.filter(r =>
        (r.nome || '').toLowerCase().includes(q) ||
        (r.cpf || '').toLowerCase().includes(q) ||
        (r.especialidade || '').toLowerCase().includes(q)
      );
      renderTable(filtered);
    }

    async function save() {
      form.classList.add('was-validated');
      if (!form.checkValidity()) return;

      const payload = {
        id: fields.id.value,
        nome: fields.nome.value,
        cpf: fields.cpf.value,
        telefone: fields.telefone.value,
        especialidade: fields.especialidade.value,
        data_contratacao: fields.data_contratacao.value,
      };

      try {
        const isUpdate = !!payload.id;
        const res = await apiPost(isUpdate ? 'update' : 'create', payload);
        modalForm.hide();
        showToast(res.message || 'Salvo com sucesso.');
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
        showToast(res.message || 'Excluído com sucesso.');
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
          const detail = await apiGet('get', { id });
          openEdit(detail);
        } catch (err) {
          showToast(err.message, true);
        }
      }

      if (action === 'delete') {
        deleteTarget = row;
        document.getElementById('delName').textContent = row.nome;
        document.getElementById('delId').textContent = row.id;
        modalDelete.show();
      }
    });

    const msg = qsParam('msg');
    const err = qsParam('erro');
    if (msg) showToast(msg);
    if (err) showToast(err, true);

    load();
  </script>

<script>

    const BASE = '<%= request.getContextPath() %>';
    const API = BASE + '/instrutores';
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
      nome: document.getElementById('nome'),
      cpf: document.getElementById('cpf'),
      telefone: document.getElementById('telefone'),
      especialidade: document.getElementById('especialidade'),
      data_contratacao: document.getElementById('data_contratacao'),
    };

    let rows = [];
    let deleteTarget = null;

    function showToast(message, isError = false) {
      toastEl.classList.remove('text-bg-danger', 'text-bg-success');
      toastEl.classList.add(isError ? 'text-bg-danger' : 'text-bg-success');
      toastMsg.textContent = message;
      toast.show();
    }

    function qsParam(name) {
      return new URLSearchParams(window.location.search).get(name);
    }

    function applyMaskCPF(value) {
      const digits = value.replace(/\D/g, '').slice(0, 11);
      const parts = [];
      parts.push(digits.slice(0, 3));
      if (digits.length >= 4) parts.push(digits.slice(3, 6));
      if (digits.length >= 7) parts.push(digits.slice(6, 9));
      let out = parts.filter(Boolean).join('.');
      if (digits.length >= 10) out += '-' + digits.slice(9, 11);
      return out;
    }

    fields.cpf.addEventListener('input', (e) => {
      const cur = e.target.value;
      e.target.value = applyMaskCPF(cur);
    });

    async function apiGet(op, params = {}) {
      const url = new URL(API, window.location.href);
      url.searchParams.set('format', 'json');
      url.searchParams.set('op', op);
      for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
      const res = await fetch(url.toString(), { headers: { 'Accept': 'application/json' } });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
      return data;
    }

    async function apiPost(op, body) {
      const res = await fetch(API + '?format=json', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 'Accept': 'application/json' },
        body: new URLSearchParams({ op, ...body }).toString()
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(data?.message || 'Erro inesperado');
      return data;
    }

    function escapeHtml(s) {
      return String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
    }

    function renderTable(list) {
      tbody.innerHTML = '';
      if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Nenhum registro.</td></tr>';
        count.textContent = '0 registros';
        return;
      }
      count.textContent = `${list.length} registro(s)`;
      for (const i of list) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td class="text-muted">${i.id}</td>
          <td>
            <div class="fw-semibold">${escapeHtml(i.nome)}</div>
            <div class="text-muted small">${escapeHtml(i.telefone || '')}</div>
          </td>
          <td>${escapeHtml(i.cpf)}</td>
          <td><span class="badge text-bg-light border">${escapeHtml(i.especialidade)}</span></td>
          <td>${escapeHtml(i.data_contratacao || '')}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${i.id}"><i class="bi bi-pencil-square"></i></button>
            <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${i.id}"><i class="bi bi-trash"></i></button>
          </td>
        `;
        tbody.appendChild(tr);
      }
    }

    function openNew() {
      document.getElementById('modalTitle').textContent = 'Novo instrutor';
      fields.id.value = '';
      for (const k of Object.keys(fields)) if (k !== 'id') fields[k].value = '';
      form.classList.remove('was-validated');
      modalForm.show();
      setTimeout(() => fields.nome.focus(), 250);
    }

    function openEdit(i) {
      document.getElementById('modalTitle').textContent = `Editar instrutor #${i.id}`;
      fields.id.value = i.id;
      fields.nome.value = i.nome || '';
      fields.cpf.value = i.cpf || '';
      fields.telefone.value = i.telefone || '';
      fields.especialidade.value = i.especialidade || '';
      fields.data_contratacao.value = i.data_contratacao || '';
      form.classList.remove('was-validated');
      modalForm.show();
    }

    async function load() {
      tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Carregando...</td></tr>';
      try {
        rows = await apiGet('list');
        applyFilter();
      } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(e.message)}</td></tr>`;
      }
    }

    function applyFilter() {
      const q = (search.value || '').toLowerCase();
      if (!q) return renderTable(rows);
      const filtered = rows.filter(r =>
        (r.nome || '').toLowerCase().includes(q) ||
        (r.cpf || '').toLowerCase().includes(q) ||
        (r.especialidade || '').toLowerCase().includes(q)
      );
      renderTable(filtered);
    }

    async function save() {
      form.classList.add('was-validated');
      if (!form.checkValidity()) return;

      const payload = {
        id: fields.id.value,
        nome: fields.nome.value,
        cpf: fields.cpf.value,
        telefone: fields.telefone.value,
        especialidade: fields.especialidade.value,
        data_contratacao: fields.data_contratacao.value,
      };

      try {
        const isUpdate = !!payload.id;
        const res = await apiPost(isUpdate ? 'update' : 'create', payload);
        modalForm.hide();
        showToast(res.message || 'Salvo com sucesso.');
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
        showToast(res.message || 'Excluído com sucesso.');
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
          const detail = await apiGet('get', { id });
          openEdit(detail);
        } catch (err) {
          showToast(err.message, true);
        }
      }

      if (action === 'delete') {
        deleteTarget = row;
        document.getElementById('delName').textContent = row.nome;
        document.getElementById('delId').textContent = row.id;
        modalDelete.show();
      }
    });

    const msg = qsParam('msg');
    const err = qsParam('erro');
    if (msg) showToast(msg);
    if (err) showToast(err, true);

    load();
  
</script>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
