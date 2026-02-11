package autoescola.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.dao.AlunoDao;
import autoescola.dao.AulaDao;
import autoescola.dao.InstrutorDao;
import autoescola.dao.VeiculoDao;
import autoescola.model.Aula;
import autoescola.model.AulaDetalhada;

/**
 * AulaServlet
 *
 * Melhorias:
 * - API JSON com listagem detalhada (JOIN) para visão/edição.
 * - Validações (inclusive relações: aluno/instrutor/veículo).
 * - Ciclo de vida: init(), service(), destroy().
 *
 * NOVO:
 * - op=future_by_aluno (JSON): lista aulas futuras MARCADAS/AGENDADAS de um aluno
 *   com JOIN (AulaDetalhada) para modal de desativação do aluno.
 */
@WebServlet("/aulas")
public class AulaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AulaDao aulaDao;
    private AlunoDao alunoDao;
    private InstrutorDao instrutorDao;
    private VeiculoDao veiculoDao;

    @Override
    public void init() throws ServletException {
        super.init();
        this.aulaDao = new AulaDao();
        this.alunoDao = new AlunoDao();
        this.instrutorDao = new InstrutorDao();
        this.veiculoDao = new VeiculoDao();
        getServletContext().log("[AulaServlet] init() - inicializado");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            super.service(req, resp);
        } finally {
            long ms = System.currentTimeMillis() - start;
            getServletContext().log("[AulaServlet] service() - " + req.getMethod() + " " + req.getRequestURI() + " (" + ms + "ms)");
        }
    }

    @Override
    public void destroy() {
        getServletContext().log("[AulaServlet] destroy() - finalizando");
        this.aulaDao = null;
        this.alunoDao = null;
        this.instrutorDao = null;
        this.veiculoDao = null;
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isJsonRequest(request)) {
            request.getRequestDispatcher("/WEB-INF/jsp/pages/aulas.jsp").forward(request, response);
            return;
        }

        String op = nvl(request.getParameter("op"), "list");

        try {
            if ("list".equalsIgnoreCase(op)) {
                List<AulaDetalhada> aulas = aulaDao.listarDetalhadas();
                // Mantém seu padrão atual (array puro) para não quebrar JS existente
                writeJson(response, 200, aulasDetalhadasToJson(aulas));
                return;
            }

            if ("get".equalsIgnoreCase(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'id' inválido."));
                    return;
                }
                Aula a = aulaDao.buscarPorId(id);
                if (a == null) {
                    writeJson(response, 404, errJson("Aula não encontrada."));
                    return;
                }
                // Mantém seu padrão atual (objeto puro)
                writeJson(response, 200, aulaToJson(a));
                return;
            }

            // ✅ NOVO: lista aulas futuras do aluno (para modal de desativação)
            if ("future_by_aluno".equalsIgnoreCase(op)) {
                Integer alunoId = parseInt(request.getParameter("aluno_id"));
                if (alunoId == null) alunoId = parseInt(request.getParameter("id")); // fallback
                if (alunoId == null || alunoId <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'aluno_id' inválido."));
                    return;
                }

                List<AulaDetalhada> futuras = aulaDao.listarFuturasMarcadasDetalhadasPorAluno(alunoId);
                String json = "{\"ok\":true,\"data\":" + aulasDetalhadasToJson(futuras) + "}";
                writeJson(response, 200, json);
                return;
            }

            writeJson(response, 400, errJson("Operação inválida: op=" + op));
        } catch (RuntimeException e) {
            writeJson(response, 500, errJson("Falha ao consultar aulas: " + safeMsg(e)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean json = isJsonRequest(request);

        String op = request.getParameter("op");
        if (op == null || op.trim().isEmpty()) {
            String acao = request.getParameter("acao");
            if ("cadastrar".equalsIgnoreCase(acao)) op = "create";
            else if ("alterar".equalsIgnoreCase(acao)) op = "update";
            else if ("remover".equalsIgnoreCase(acao)) op = "delete";
        }
        op = nvl(op, "").toLowerCase();

        try {
            if ("create".equals(op)) {
                Aula a = readAulaFromRequest(request, false);
                validateAula(a, false);
                aulaDao.inserir(a);
                respond(response, json, 200, okJson("Aula cadastrada com sucesso."), "aulas?msg=Aula+cadastrada");
                return;
            }

            if ("update".equals(op)) {
                Aula a = readAulaFromRequest(request, true);
                validateAula(a, true);

                Aula existente = aulaDao.buscarPorId(a.getId());
                if (existente == null) {
                    respond(response, json, 404, errJson("Aula não encontrada."), "aulas?erro=Aula+não+encontrada");
                    return;
                }
                aulaDao.alterar(a);
                respond(response, json, 200, okJson("Aula atualizada com sucesso."), "aulas?msg=Aula+atualizada");
                return;
            }

            if ("delete".equals(op)) {
                Integer id = parseInt(nvl(request.getParameter("id"), request.getParameter("id_remover")));
                if (id == null || id <= 0) {
                    respond(response, json, 400, errJson("ID inválido."), "aulas?erro=ID+inválido");
                    return;
                }
                Aula existente = aulaDao.buscarPorId(id);
                if (existente == null) {
                    respond(response, json, 404, errJson("Aula não encontrada."), "aulas?erro=Aula+não+encontrada");
                    return;
                }
                aulaDao.remover(id);
                respond(response, json, 200, okJson("Aula removida com sucesso."), "aulas?msg=Aula+removida");
                return;
            }

            respond(response, json, 400, errJson("Operação inválida."), "aulas?erro=Operação+inválida");
        } catch (IllegalArgumentException e) {
            respond(response, json, 400, errJson(safeMsg(e)), "aulas?erro=" + urlEncode(safeMsg(e)));
        } catch (RuntimeException e) {
            respond(response, json, 500, errJson("Falha ao processar: " + safeMsg(e)), "aulas?erro=Falha+ao+processar");
        }
    }

    // --------------------------
    // Leitura/Validação
    // --------------------------
    private Aula readAulaFromRequest(HttpServletRequest request, boolean requireId) {
        Aula a = new Aula();

        if (requireId) {
            Integer id = parseInt(request.getParameter("id"));
            if (id == null || id <= 0) throw new IllegalArgumentException("Informe um ID válido para editar.");
            a.setId(id);
        }

        Integer alunoId = parseInt(request.getParameter("aluno_id"));
        Integer instrutorId = parseInt(request.getParameter("instrutor_id"));
        Integer veiculoId = parseInt(request.getParameter("veiculo_id"));

        a.setAlunoId(alunoId);
        a.setInstrutorId(instrutorId);
        a.setVeiculoId(veiculoId); // pode ser null

        String data = trim(request.getParameter("data_aula"));
        if (!data.isEmpty()) {
            // datetime-local vem como yyyy-MM-ddTHH:mm
            a.setDataAula(LocalDateTime.parse(data));
        }

        Integer dur = parseInt(request.getParameter("duracao_minutos"));
        if (dur != null) a.setDuracaoMinutos(dur);

        a.setTipo(trim(request.getParameter("tipo")));
        a.setStatus(trim(request.getParameter("status")));
        a.setObservacoes(trim(request.getParameter("observacoes")));

        return a;
    }

    private void validateAula(Aula a, boolean requireId) {
        if (requireId && (a.getId() == null || a.getId() <= 0)) throw new IllegalArgumentException("ID inválido.");
        if (a.getAlunoId() == null || a.getAlunoId() <= 0) throw new IllegalArgumentException("Aluno é obrigatório.");
        if (a.getInstrutorId() == null || a.getInstrutorId() <= 0) throw new IllegalArgumentException("Instrutor é obrigatório.");
        if (a.getDataAula() == null) throw new IllegalArgumentException("Data e hora são obrigatórias.");
        if (a.getDuracaoMinutos() <= 0) throw new IllegalArgumentException("Duração inválida.");
        if (isBlank(a.getTipo())) throw new IllegalArgumentException("Tipo é obrigatório.");
        if (isBlank(a.getStatus())) throw new IllegalArgumentException("Status é obrigatório.");

        String tipo = a.getTipo().toUpperCase();
        if (!"PRATICA".equals(tipo) && !"TEORICA".equals(tipo)) {
            throw new IllegalArgumentException("Tipo inválido. Use PRATICA ou TEORICA.");
        }
        if ("PRATICA".equals(tipo) && (a.getVeiculoId() == null || a.getVeiculoId() <= 0)) {
            throw new IllegalArgumentException("Para aula PRÁTICA, informe o veículo.");
        }

        // Valida relacionamentos (mensagens mais amigáveis)
        if (alunoDao.buscarPorId(a.getAlunoId()) == null) {
            throw new IllegalArgumentException("Aluno não encontrado (ID=" + a.getAlunoId() + ").");
        }
        if (instrutorDao.buscarPorId(a.getInstrutorId()) == null) {
            throw new IllegalArgumentException("Instrutor não encontrado (ID=" + a.getInstrutorId() + ").");
        }
        if (a.getVeiculoId() != null && a.getVeiculoId() > 0) {
            if (veiculoDao.buscarPorId(a.getVeiculoId()) == null) {
                throw new IllegalArgumentException("Veículo não encontrado (ID=" + a.getVeiculoId() + ").");
            }
        }
    }

    // --------------------------
    // JSON helpers
    // --------------------------
    private String aulasDetalhadasToJson(List<AulaDetalhada> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(aulaDetalhadaToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String aulaDetalhadaToJson(AulaDetalhada a) {
        String veiculo;
        if (a.getVeiculoModelo() != null && !a.getVeiculoModelo().trim().isEmpty()) {
            veiculo = a.getVeiculoModelo() + (a.getVeiculoPlaca() != null ? " (" + a.getVeiculoPlaca() + ")" : "");
        } else {
            veiculo = (a.getVeiculoId() == null ? "N/A (Teórica)" : "N/A");
        }

        return "{" +
                "\"id\":" + a.getId() + "," +
                "\"aluno_id\":" + a.getAlunoId() + "," +
                "\"aluno_nome\":\"" + j(a.getAlunoNome()) + "\"," +
                "\"instrutor_id\":" + a.getInstrutorId() + "," +
                "\"instrutor_nome\":\"" + j(a.getInstrutorNome()) + "\"," +
                "\"veiculo_id\":" + (a.getVeiculoId() == null ? "null" : a.getVeiculoId()) + "," +
                "\"veiculo\":\"" + j(veiculo) + "\"," +
                "\"data_aula\":\"" + j(a.getDataAula() != null ? a.getDataAula().toString() : "") + "\"," +
                "\"duracao_minutos\":" + a.getDuracaoMinutos() + "," +
                "\"tipo\":\"" + j(a.getTipo()) + "\"," +
                "\"status\":\"" + j(a.getStatus()) + "\"," +
                "\"observacoes\":\"" + j(nvl(a.getObservacoes(), "")) + "\"" +
                "}";
    }

    private String aulaToJson(Aula a) {
        return "{" +
                "\"id\":" + a.getId() + "," +
                "\"aluno_id\":" + a.getAlunoId() + "," +
                "\"instrutor_id\":" + a.getInstrutorId() + "," +
                "\"veiculo_id\":" + (a.getVeiculoId() == null ? "null" : a.getVeiculoId()) + "," +
                "\"data_aula\":\"" + j(a.getDataAula() != null ? a.getDataAula().toString() : "") + "\"," +
                "\"duracao_minutos\":" + a.getDuracaoMinutos() + "," +
                "\"tipo\":\"" + j(a.getTipo()) + "\"," +
                "\"status\":\"" + j(a.getStatus()) + "\"," +
                "\"observacoes\":\"" + j(nvl(a.getObservacoes(), "")) + "\"" +
                "}";
    }

    private String okJson(String msg) { return "{\"ok\":true,\"message\":\"" + j(msg) + "\"}"; }
    private String errJson(String msg) { return "{\"ok\":false,\"message\":\"" + j(msg) + "\"}"; }

    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(json);
    }

    private void respond(HttpServletResponse resp, boolean json, int status, String jsonBody, String redirectUrl) throws IOException {
        if (json) writeJson(resp, status, jsonBody);
        else resp.sendRedirect(redirectUrl);
    }

    private boolean isJsonRequest(HttpServletRequest req) {
        String format = req.getParameter("format");
        if (format != null && format.equalsIgnoreCase("json")) return true;
        String accept = req.getHeader("Accept");
        return accept != null && accept.toLowerCase().contains("application/json");
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static Integer parseInt(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String nvl(String s, String def) { return (s == null || s.trim().isEmpty()) ? def : s.trim(); }

    private static String j(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        return m == null ? "Erro inesperado" : m;
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
