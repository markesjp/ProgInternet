<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  request.setAttribute("pageTitle", "Alunos - Autoescola");
%>

<jsp:include page="/WEB-INF/jsp/components/header.jsp" />
<jsp:include page="/WEB-INF/jsp/components/menu.jsp" />

<main class="container py-4">
  <div class="d-flex flex-column flex-md-row gap-2 align-items-md-center justify-content-between mb-3">
    <div>
      <h1 class="h4 mb-1">Alunos</h1>
      <div class="text-muted small">Dica: clique em <code class="kbd">Editar</code> para carregar os dados no formulário.</div>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-outline-secondary" id="btnReload">
        <i class="bi bi-arrow-clockwise me-1"></i>Atualizar
      </button>
      <button class="btn btn-primary" id="btnNew">
        <i class="bi bi-plus-lg me-1"></i>Novo aluno
      </button>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-body">
      <div class="row g-2 align-items-center mb-3">
        <div class="col-12 col-md-6">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input class="form-control" id="search" placeholder="Buscar por nome, CPF ou categoria..." />
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
              <th>Categoria</th>
              <th>Matrícula</th>
              <th style="width:190px" class="text-end"></th>
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

<!-- Modal: Form (Create/Update) -->
<div class="modal fade" id="modalForm" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modalTitle">Aluno</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>

      <form class="modal-body needs-validation" id="form" novalidate>
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

          <div class="col-12 col-md-8">
            <label class="form-label">Email</label>
            <input class="form-control" id="email" type="email" placeholder="exemplo@dominio.com" />
            <div class="invalid-feedback">Email inválido.</div>
          </div>

          <div class="col-12 col-md-4">
            <label class="form-label required">Nascimento</label>
            <input class="form-control" id="data_nascimento" type="date" required />
            <div class="invalid-feedback">Informe a data de nascimento.</div>
          </div>

          <div class="col-12 col-md-4">
            <label class="form-label required">Categoria</label>
            <select class="form-select" id="categoria_desejada" required>
              <option value="">Selecione...</option>
              <option value="A">A</option>
              <option value="B">B</option>
              <option value="AB">AB</option>
              <option value="C">C</option>
              <option value="D">D</option>
              <option value="E">E</option>
            </select>
            <div class="invalid-feedback">Selecione a categoria.</div>
          </div>

          <div class="col-12 col-md-4">
            <label class="form-label required">Matrícula</label>
            <input class="form-control" id="data_matricula" type="date" required />
            <div class="invalid-feedback">Informe a data de matrícula.</div>
          </div>
        </div>
      </form>

      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="button" class="btn btn-primary" id="btnSave">
          <i class="bi bi-save me-1"></i>Salvar
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Modal: Confirmar Desativação/Ativação -->
<div class="modal fade" id="modalStatus" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modalStatusTitle">Confirmar ação</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>

      <div class="modal-body">
        <div class="alert alert-warning mb-3" id="modalStatusWarn" style="display:none;">
          <strong>Atenção:</strong> ao desativar este aluno, todas as aulas futuras <b>MARCADAS</b> serão automaticamente
          <b>DESMARCADAS</b>, mantendo histórico.
          <div class="small text-muted mt-1" id="modalAulasInfo"></div>
        </div>

        <div class="mb-3">
          <label class="form-label">Motivo (opcional, recomendado)</label>
          <textarea id="motivoStatus" class="form-control" rows="2" maxlength="255"
            placeholder="Ex.: aluno solicitou pausa / desistência / pendência documental..."></textarea>
          <div class="form-text">Esse motivo fica registrado no histórico.</div>
        </div>

        <input type="hidden" id="statusTargetId" />
        <input type="hidden" id="statusTargetAction" />
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="button" class="btn btn-warning" id="btnConfirmStatus">
          <i class="bi bi-check2-circle me-1"></i>Confirmar
        </button>
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
  const BASE = '<%= request.getContextPath() %>';
  const API = BASE + '/alunos';

  const tbody = document.getElementById('tbody');
  const search = document.getElementById('search');
  const count = document.getElementById('count');

  const modalForm = new bootstrap.Modal(document.getElementById('modalForm'));
  const modalStatus = new bootstrap.Modal(document.getElementById('modalStatus'));

  const toastEl = document.getElementById('toast');
  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  const toastMsg = document.getElementById('toastMsg');

  const form = document.getElementById('form');

  const fields = {
    id: document.getElementById('id'),
    nome: document.getElementById('nome'),
    cpf: document.getElementById('cpf'),
    telefone: document.getElementById('telefone'),
    email: document.getElementById('email'),
    data_nascimento: document.getElementById('data_nascimento'),
    categoria_desejada: document.getElementById('categoria_desejada'),
    data_matricula: document.getElementById('data_matricula'),
  };

  let rows = [];
  let statusTarget = null; // {id, action, nome, status}

  function showToast(message, isError = false) {
    toastEl.classList.remove('text-bg-danger', 'text-bg-success');
    toastEl.classList.add(isError ? 'text-bg-danger' : 'text-bg-success');
    toastMsg.textContent = message;
    toast.show();
  }

  function qsParam(name) {
    return new URLSearchParams(window.location.search).get(name);
  }

  function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, (c) => ({
      '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
    }[c]));
  }

  function statusBadge(status) {
    const st = String(status || 'ATIVO').toUpperCase();
    const cls = st === 'ATIVO' ? 'bg-success' : 'bg-secondary';
    return `<span class="badge ${cls} ms-2">${escapeHtml(st)}</span>`;
  }

  function applyMaskCPF(value) {
    const digits = value.replace(/\D/g, '').slice(0, 11);
    const p1 = digits.slice(0, 3);
    const p2 = digits.slice(3, 6);
    const p3 = digits.slice(6, 9);
    const p4 = digits.slice(9, 11);

    let out = p1;
    if (p2) out += '.' + p2;
    if (p3) out += '.' + p3;
    if (p4) out += '-' + p4;
    return out;
  }

  fields.cpf.addEventListener('input', (e) => {
    e.target.value = applyMaskCPF(e.target.value);
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

  function renderTable(list) {
    tbody.innerHTML = '';

    if (!list.length) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Nenhum registro.</td></tr>';
      count.textContent = '0 registros';
      return;
    }

    count.textContent = `${list.length} registro(s)`;

    for (const a of list) {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="text-muted">${a.id}</td>
        <td>
          <div class="fw-semibold">${escapeHtml(a.nome)} ${statusBadge(a.status)}</div>
          <div class="text-muted small">${escapeHtml(a.email || '')}</div>
        </td>
        <td>${escapeHtml(a.cpf)}</td>
        <td><span class="badge text-bg-light border">${escapeHtml(a.categoria_desejada)}</span></td>
        <td>${escapeHtml(a.data_matricula)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${a.id}" title="Editar">
            <i class="bi bi-pencil-square"></i>
          </button>

          ${String((a.status||'ATIVO')).toUpperCase() === 'INATIVO'
            ? `<button class="btn btn-sm btn-outline-success" data-action="activate" data-id="${a.id}" title="Ativar">
                 <i class="bi bi-person-check"></i>
               </button>`
            : `<button class="btn btn-sm btn-outline-warning" data-action="deactivate" data-id="${a.id}" title="Desativar">
                 <i class="bi bi-person-x"></i>
               </button>`
          }
        </td>
      `;
      tbody.appendChild(tr);
    }
  }

  function openNew() {
    document.getElementById('modalTitle').textContent = 'Novo aluno';
    fields.id.value = '';
    for (const k of Object.keys(fields)) if (k !== 'id') fields[k].value = '';
    form.classList.remove('was-validated');
    modalForm.show();
    setTimeout(() => fields.nome.focus(), 250);
  }

  function openEdit(a) {
    document.getElementById('modalTitle').textContent = `Editar aluno #${a.id}`;
    fields.id.value = a.id;
    fields.nome.value = a.nome || '';
    fields.cpf.value = a.cpf || '';
    fields.telefone.value = a.telefone || '';
    fields.email.value = a.email || '';
    fields.data_nascimento.value = a.data_nascimento || '';
    fields.categoria_desejada.value = a.categoria_desejada || '';
    fields.data_matricula.value = a.data_matricula || '';
    form.classList.remove('was-validated');
    modalForm.show();
  }

  async function load() {
    tbody.innerHTML = '<tr><td colspan="6" class="text-muted">Carregando...</td></tr>';
    try {
      const resp = await apiGet('list');
      // servlet -> {ok:true,data:[...]}
      rows = resp.data || [];
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
      (r.categoria_desejada || '').toLowerCase().includes(q)
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
      email: fields.email.value,
      data_nascimento: fields.data_nascimento.value,
      categoria_desejada: fields.categoria_desejada.value,
      data_matricula: fields.data_matricula.value,
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

  async function openStatusModal(row, action) {
    statusTarget = { id: row.id, action, nome: row.nome, status: row.status };

    document.getElementById('statusTargetId').value = row.id;
    document.getElementById('statusTargetAction').value = action;
    document.getElementById('motivoStatus').value = '';

    const title = action === 'deactivate' ? 'Confirmar desativação' : 'Confirmar ativação';
    document.getElementById('modalStatusTitle').textContent = title;

    const warn = document.getElementById('modalStatusWarn');
    const info = document.getElementById('modalAulasInfo');
    const btn = document.getElementById('btnConfirmStatus');

    if (action === 'deactivate') {
      warn.style.display = '';
      info.textContent = 'Carregando...';

      btn.className = 'btn btn-warning';
      btn.innerHTML = '<i class="bi bi-person-x me-1"></i>Desativar';

      try {
        const r = await apiGet('future_count', { id: row.id });
        const qtd = (r && typeof r.count !== 'undefined') ? r.count : 0;
        info.textContent = `Aulas futuras marcadas a desmarcar: ${qtd}`;
      } catch {
        info.textContent = '';
      }
    } else {
      warn.style.display = 'none';
      info.textContent = '';

      btn.className = 'btn btn-success';
      btn.innerHTML = '<i class="bi bi-person-check me-1"></i>Ativar';
    }

    modalStatus.show();
  }

  async function confirmStatus() {
    if (!statusTarget) return;

    const motivo = document.getElementById('motivoStatus').value || '';
    try {
      if (statusTarget.action === 'deactivate') {
        const res = await apiPost('deactivate', { id: statusTarget.id, confirm: true, motivo });
        modalStatus.hide();
        showToast(res.message || 'Aluno desativado.');
      } else if (statusTarget.action === 'activate') {
        const res = await apiPost('activate', { id: statusTarget.id, motivo });
        modalStatus.hide();
        showToast(res.message || 'Aluno ativado.');
      }
      await load();
    } catch (e) {
      showToast(e.message, true);
    } finally {
      statusTarget = null;
      document.getElementById('motivoStatus').value = '';
    }
  }

  document.getElementById('btnNew').addEventListener('click', openNew);
  document.getElementById('btnReload').addEventListener('click', load);
  document.getElementById('btnSave').addEventListener('click', save);
  document.getElementById('btnConfirmStatus').addEventListener('click', confirmStatus);
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
      return;
    }

    if (action === 'deactivate' || action === 'activate') {
      await openStatusModal(row, action);
      return;
    }
  });

  const msg = qsParam('msg');
  const err = qsParam('erro');
  if (msg) showToast(msg);
  if (err) showToast(err, true);

  load();
</script>

<jsp:include page="/WEB-INF/jsp/components/footer.jsp" />
