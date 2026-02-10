package autoescola.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.dao.InstrutorDao;
import autoescola.model.Instrutor;

/**
 * InstrutorServlet
 *
 * Melhorias:
 * - API JSON para interface moderna (tabela + edição + exclusão).
 * - Validações e tratamento de erros.
 * - Ciclo de vida: init(), service(), destroy().
 */
@WebServlet("/instrutores")
public class InstrutorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private InstrutorDao dao;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dao = new InstrutorDao();
        getServletContext().log("[InstrutorServlet] init() - inicializado");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            super.service(req, resp);
        } finally {
            long ms = System.currentTimeMillis() - start;
            getServletContext().log("[InstrutorServlet] service() - " + req.getMethod() + " " + req.getRequestURI() + " (" + ms + "ms)");
        }
    }

    @Override
    public void destroy() {
        getServletContext().log("[InstrutorServlet] destroy() - finalizando");
        this.dao = null;
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isJsonRequest(request)) {
            request.getRequestDispatcher("/WEB-INF/jsp/pages/instrutores.jsp").forward(request, response);
        return;
}

        String op = nvl(request.getParameter("op"), "list");

        try {
            if ("list".equalsIgnoreCase(op)) {
                List<Instrutor> instrutores = dao.listar();
                writeJson(response, 200, instrutoresToJson(instrutores));
                return;
            }

            if ("get".equalsIgnoreCase(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'id' inválido."));
                    return;
                }
                Instrutor i = dao.buscarPorId(id);
                if (i == null) {
                    writeJson(response, 404, errJson("Instrutor não encontrado."));
                    return;
                }
                writeJson(response, 200, instrutorToJson(i));
                return;
            }

            writeJson(response, 400, errJson("Operação inválida: op=" + op));
        } catch (RuntimeException e) {
            writeJson(response, 500, errJson("Falha ao consultar instrutores: " + safeMsg(e)));
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
                Instrutor instrutor = readInstrutorFromRequest(request, false);
                validateInstrutor(instrutor, false);

                if (dao.existeCpf(instrutor.getCpf())) {
                    respond(response, json, 409, errJson("CPF já cadastrado."), "instrutores?erro=CPF+já+cadastrado");
                    return;
                }

                dao.inserir(instrutor);
                respond(response, json, 200, okJson("Instrutor cadastrado com sucesso."), "instrutores?msg=Instrutor+cadastrado");
                return;
            }

            if ("update".equals(op)) {
                Instrutor instrutor = readInstrutorFromRequest(request, true);
                validateInstrutor(instrutor, true);

                Instrutor existente = dao.buscarPorId(instrutor.getId());
                if (existente == null) {
                    respond(response, json, 404, errJson("Instrutor não encontrado."), "instrutores?erro=Instrutor+não+encontrado");
                    return;
                }

                if (!safeEquals(existente.getCpf(), instrutor.getCpf()) && dao.existeCpf(instrutor.getCpf())) {
                    respond(response, json, 409, errJson("CPF já cadastrado."), "instrutores?erro=CPF+já+cadastrado");
                    return;
                }

                dao.alterar(instrutor);
                respond(response, json, 200, okJson("Instrutor atualizado com sucesso."), "instrutores?msg=Instrutor+atualizado");
                return;
            }

            if ("delete".equals(op)) {
                Integer id = parseInt(nvl(request.getParameter("id"), request.getParameter("id_remover")));
                if (id == null || id <= 0) {
                    respond(response, json, 400, errJson("ID inválido."), "instrutores?erro=ID+inválido");
                    return;
                }
                Instrutor existente = dao.buscarPorId(id);
                if (existente == null) {
                    respond(response, json, 404, errJson("Instrutor não encontrado."), "instrutores?erro=Instrutor+não+encontrado");
                    return;
                }
                dao.remover(id);
                respond(response, json, 200, okJson("Instrutor removido com sucesso."), "instrutores?msg=Instrutor+removido");
                return;
            }

            respond(response, json, 400, errJson("Operação inválida."), "instrutores?erro=Operação+inválida");
        } catch (IllegalArgumentException e) {
            respond(response, json, 400, errJson(safeMsg(e)), "instrutores?erro=" + urlEncode(safeMsg(e)));
        } catch (RuntimeException e) {
            respond(response, json, 500, errJson("Falha ao processar: " + safeMsg(e)), "instrutores?erro=Falha+ao+processar");
        }
    }

    private Instrutor readInstrutorFromRequest(HttpServletRequest request, boolean requireId) {
        Instrutor i = new Instrutor();

        if (requireId) {
            Integer id = parseInt(request.getParameter("id"));
            if (id == null || id <= 0) throw new IllegalArgumentException("Informe um ID válido para editar.");
            i.setId(id);
        }

        i.setNome(trim(request.getParameter("nome")));
        i.setCpf(trim(request.getParameter("cpf")));
        i.setTelefone(trim(request.getParameter("telefone")));
        i.setEspecialidade(trim(request.getParameter("especialidade")));

        String dc = trim(request.getParameter("data_contratacao"));
        if (!dc.isEmpty()) {
            i.setDataContratacao(LocalDate.parse(dc));
        }

        return i;
    }

    private void validateInstrutor(Instrutor i, boolean requireId) {
        if (requireId && (i.getId() == null || i.getId() <= 0)) throw new IllegalArgumentException("ID inválido.");
        if (isBlank(i.getNome()) || i.getNome().length() < 2) throw new IllegalArgumentException("Nome é obrigatório (mín. 2 caracteres).");
        if (isBlank(i.getCpf())) throw new IllegalArgumentException("CPF é obrigatório.");
        if (isBlank(i.getEspecialidade())) throw new IllegalArgumentException("Especialidade é obrigatória.");
        if (i.getDataContratacao() == null) throw new IllegalArgumentException("Data de contratação é obrigatória.");
    }

    private String instrutoresToJson(List<Instrutor> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int x = 0; x < list.size(); x++) {
            if (x > 0) sb.append(",");
            sb.append(instrutorToJson(list.get(x)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String instrutorToJson(Instrutor i) {
        return "{" +
                "\"id\":" + i.getId() + "," +
                "\"nome\":\"" + j(i.getNome()) + "\"," +
                "\"cpf\":\"" + j(i.getCpf()) + "\"," +
                "\"telefone\":\"" + j(nvl(i.getTelefone(), "")) + "\"," +
                "\"especialidade\":\"" + j(i.getEspecialidade()) + "\"," +
                "\"data_contratacao\":\"" + j(i.getDataContratacao() != null ? i.getDataContratacao().toString() : "") + "\"" +
                "}";
    }

    private String okJson(String msg) {
        return "{\"ok\":true,\"message\":\"" + j(msg) + "\"}";
    }

    private String errJson(String msg) {
        return "{\"ok\":false,\"message\":\"" + j(msg) + "\"}";
    }

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

    private static boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
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
