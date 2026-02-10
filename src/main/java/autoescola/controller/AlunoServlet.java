package autoescola.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.dao.AlunoDao;
import autoescola.model.Aluno;

/**
 * AlunoServlet
 *
 * - View em JSP (quando não for JSON)
 * - API JSON para CRUD + status
 * - Ciclo de vida: init(), service() e destroy()
 */
@WebServlet("/alunos")
public class AlunoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AlunoDao dao;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dao = new AlunoDao();
        getServletContext().log("[AlunoServlet] init()");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long t0 = System.currentTimeMillis();
        try {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            super.service(request, response);
        } finally {
            getServletContext().log("[AlunoServlet] " + request.getMethod() + " " + request.getRequestURI()
                    + " " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isJsonRequest(request)) {
            request.getRequestDispatcher("/WEB-INF/jsp/pages/alunos.jsp").forward(request, response);
            return;
        }

        String op = nvl(request.getParameter("op"), "list");

        try {
            if ("list".equalsIgnoreCase(op)) {
                List<Aluno> alunos = dao.listar();
                writeJson(response, 200, alunosToJson(alunos));
                return;
            }

            if ("get".equalsIgnoreCase(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'id' inválido."));
                    return;
                }
                Aluno a = dao.buscarPorId(id);
                if (a == null) {
                    writeJson(response, 404, errJson("Aluno não encontrado."));
                    return;
                }
                writeJson(response, 200, alunoToJson(a));
                return;
            }

            if ("future_count".equalsIgnoreCase(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'id' inválido."));
                    return;
                }
                int count = dao.contarAulasFuturasMarcadas(id);
                writeJson(response, 200, "{\"ok\":true,\"count\":" + count + "}");
                return;
            }

            writeJson(response, 400, errJson("Operação inválida: op=" + op));
        } catch (RuntimeException e) {
            writeJson(response, 500, errJson("Falha ao consultar alunos: " + safeMsg(e)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean json = isJsonRequest(request);

        // Compatibilidade: HTML antigo usa name="acao".
        String op = request.getParameter("op");
        if (op == null || op.trim().isEmpty()) {
            String acao = request.getParameter("acao");
            if ("cadastrar".equalsIgnoreCase(acao)) op = "create";
            else if ("alterar".equalsIgnoreCase(acao)) op = "update";
            else if ("remover".equalsIgnoreCase(acao)) op = "delete";
        }
        op = nvl(op, "").trim().toLowerCase();

        try {
            // =========================
            // CREATE
            // =========================
            if ("create".equals(op)) {
                Aluno aluno = readAlunoFromRequest(request, false);
                validateAluno(aluno, false);

                if (dao.existeCpf(aluno.getCpf())) {
                    respond(response, json, 409,
                            errJson("CPF já cadastrado."),
                            redirect(request, "alunos?erro=" + urlEncode("CPF já cadastrado.")));
                    return;
                }

                aluno.setStatus("ATIVO");
                dao.inserir(aluno);

                respond(response, json, 200,
                        okJson("Aluno cadastrado com sucesso."),
                        redirect(request, "alunos?msg=" + urlEncode("Aluno cadastrado com sucesso.")));
                return;
            }

            // =========================
            // UPDATE
            // =========================
            if ("update".equals(op)) {
                Aluno aluno = readAlunoFromRequest(request, true);
                validateAluno(aluno, true);

                Aluno existente = dao.buscarPorId(aluno.getId());
                if (existente == null) {
                    respond(response, json, 404,
                            errJson("Aluno não encontrado."),
                            redirect(request, "alunos?erro=" + urlEncode("Aluno não encontrado.")));
                    return;
                }

                // Se trocar CPF, precisa checar duplicidade
                if (!safeEquals(existente.getCpf(), aluno.getCpf()) && dao.existeCpf(aluno.getCpf())) {
                    respond(response, json, 409,
                            errJson("CPF já cadastrado."),
                            redirect(request, "alunos?erro=" + urlEncode("CPF já cadastrado.")));
                    return;
                }

                dao.alterar(aluno);

                respond(response, json, 200,
                        okJson("Aluno atualizado com sucesso."),
                        redirect(request, "alunos?msg=" + urlEncode("Aluno atualizado com sucesso.")));
                return;
            }

            // =========================
            // DESATIVAR (delete/deactivate)
            // =========================
            if ("deactivate".equals(op) || "delete".equals(op)) {
                Integer id = parseInt(nvl(request.getParameter("id"), request.getParameter("id_remover")));
                if (id == null || id <= 0) {
                    respond(response, json, 400,
                            errJson("ID inválido."),
                            redirect(request, "alunos?erro=" + urlEncode("ID inválido.")));
                    return;
                }

                Aluno existente = dao.buscarPorId(id);
                if (existente == null) {
                    respond(response, json, 404,
                            errJson("Aluno não encontrado."),
                            redirect(request, "alunos?erro=" + urlEncode("Aluno não encontrado.")));
                    return;
                }

                boolean confirm = "true".equalsIgnoreCase(request.getParameter("confirm"));
                String motivo = nvl(request.getParameter("motivo"), "").trim();

                if (!confirm) {
                    int count = dao.contarAulasFuturasMarcadas(id);
                    String payload = "{\"ok\":false,\"requiresConfirm\":true,\"count\":" + count
                            + ",\"message\":\"Confirme para desativar. Isso irá desmarcar " + count + " aula(s) futura(s) marcada(s).\"}";
                    // JSON: 409 para o front abrir modal.
                    // HTML: redireciona com mensagem (o JSP pode mostrar e abrir modal).
                    respond(response, json, 409,
                            payload,
                            redirect(request, "alunos?erro=" + urlEncode("Confirmação necessária para desativar (desmarca " + count + " futuras).")
                                    + "&confirmRequired=true&id=" + id + "&count=" + count));
                    return;
                }

                int desmarcadas = dao.desativarComDesmarcacaoFuturas(id, motivo, "user");

                String okPayload = "{\"ok\":true,\"message\":\"Aluno desativado. Aulas futuras desmarcadas: "
                        + desmarcadas + ".\",\"desmarcadas\":" + desmarcadas + "}";

                respond(response, json, 200,
                        okPayload,
                        redirect(request, "alunos?msg=" + urlEncode("Aluno desativado. Aulas futuras desmarcadas: " + desmarcadas + ".")));
                return;
            }

            // =========================
            // ATIVAR
            // =========================
            if ("activate".equals(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    respond(response, json, 400,
                            errJson("ID inválido."),
                            redirect(request, "alunos?erro=" + urlEncode("ID inválido.")));
                    return;
                }

                Aluno existente = dao.buscarPorId(id);
                if (existente == null) {
                    respond(response, json, 404,
                            errJson("Aluno não encontrado."),
                            redirect(request, "alunos?erro=" + urlEncode("Aluno não encontrado.")));
                    return;
                }

                String motivo = nvl(request.getParameter("motivo"), "").trim();
                dao.ativar(id, motivo, "user");

                respond(response, json, 200,
                        okJson("Aluno ativado com sucesso."),
                        redirect(request, "alunos?msg=" + urlEncode("Aluno ativado com sucesso.")));
                return;
            }

            respond(response, json, 400,
                    errJson("Operação inválida."),
                    redirect(request, "alunos?erro=" + urlEncode("Operação inválida.")));

        } catch (IllegalArgumentException e) {
            respond(response, json, 400,
                    errJson(safeMsg(e)),
                    redirect(request, "alunos?erro=" + urlEncode(safeMsg(e))));
        } catch (RuntimeException e) {
            respond(response, json, 500,
                    errJson("Falha ao processar: " + safeMsg(e)),
                    redirect(request, "alunos?erro=" + urlEncode("Falha ao processar.")));
        }
    }

    @Override
    public void destroy() {
        getServletContext().log("[AlunoServlet] destroy()");
        this.dao = null;
        super.destroy();
    }

    // ---------------------------
    // Parsing/validação
    // ---------------------------
    private Aluno readAlunoFromRequest(HttpServletRequest request, boolean requireId) {
        Aluno a = new Aluno();

        if (requireId) {
            Integer id = parseInt(nvl(request.getParameter("id"), request.getParameter("id_alterar")));
            if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
            a.setId(id);
        }

        String nome = nvl(request.getParameter("nome"), "").trim();
        String cpf = nvl(request.getParameter("cpf"), "").replaceAll("\\D", "");
        String telefone = nvl(request.getParameter("telefone"), "").trim();
        String email = nvl(request.getParameter("email"), "").trim();
        String categoria = nvl(request.getParameter("categoria_desejada"), "").trim().toUpperCase();

        String dn = nvl(request.getParameter("data_nascimento"), "").trim();

        a.setNome(nome);
        a.setCpf(cpf);
        a.setTelefone(telefone);
        a.setEmail(email);
        a.setCategoriaDesejada(categoria);

        if (!dn.isEmpty()) a.setDataNascimento(LocalDate.parse(dn));
        a.setDataMatricula(LocalDate.now());

        return a;
    }

    private void validateAluno(Aluno a, boolean isUpdate) {
        if (a.getNome() == null || a.getNome().trim().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres.");
        }
        if (a.getCpf() == null || a.getCpf().length() != 11) {
            throw new IllegalArgumentException("CPF inválido (precisa ter 11 dígitos).");
        }
        if (a.getCategoriaDesejada() == null || a.getCategoriaDesejada().isBlank()) {
            throw new IllegalArgumentException("Categoria desejada é obrigatória.");
        }

        // valida categorias comuns (ajuste se seu sistema aceitar outras)
        String c = a.getCategoriaDesejada().trim().toUpperCase();
        if (!(c.equals("A") || c.equals("B") || c.equals("AB") || c.equals("ACC") || c.equals("AC") || c.equals("AD") || c.equals("AE"))) {
            // não trava se você quiser ser permissivo: comente esse if
            // mas para "profissional", vale validar
            // throw new IllegalArgumentException("Categoria desejada inválida (ex.: A, B, AB, ACC).");
        }

        if (a.getEmail() != null && !a.getEmail().isBlank() && !a.getEmail().contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
    }

    // ---------------------------
    // JSON helpers
    // ---------------------------
    private static boolean isJsonRequest(HttpServletRequest request) {
        String format = request.getParameter("format");
        if (format != null && "json".equalsIgnoreCase(format)) return true;

        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains("application/json")) return true;

        String ct = request.getContentType();
        return ct != null && ct.toLowerCase().contains("application/json");
    }

    private static void writeJson(HttpServletResponse response, int status, String payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(payload);
        }
    }

    private static String okJson(String msg) {
        return "{\"ok\":true,\"message\":\"" + jsonEscape(msg) + "\"}";
    }

    private static String errJson(String msg) {
        return "{\"ok\":false,\"message\":\"" + jsonEscape(msg) + "\"}";
    }

    private static String alunoToJson(Aluno a) {
        String status = a.getStatus();
        if (status == null || status.isBlank()) status = "ATIVO";
        return "{"
            + "\"id\":" + a.getId() + ","
            + "\"nome\":\"" + jsonEscape(a.getNome()) + "\","
            + "\"cpf\":\"" + jsonEscape(a.getCpf()) + "\","
            + "\"telefone\":\"" + jsonEscape(nvl(a.getTelefone())) + "\","
            + "\"email\":\"" + jsonEscape(nvl(a.getEmail())) + "\","
            + "\"data_nascimento\":\"" + jsonEscape(a.getDataNascimento() != null ? a.getDataNascimento().toString() : "") + "\","
            + "\"categoria_desejada\":\"" + jsonEscape(nvl(a.getCategoriaDesejada())) + "\","
            + "\"data_matricula\":\"" + jsonEscape(a.getDataMatricula() != null ? a.getDataMatricula().toString() : "") + "\","
            + "\"status\":\"" + jsonEscape(status) + "\""
            + "}";
    }

    private static String alunosToJson(List<Aluno> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"data\":[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(alunoToJson(list.get(i)));
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void respond(HttpServletResponse response, boolean json, int status, String jsonBody, String redirectUrl)
            throws IOException {
        if (json) {
            writeJson(response, status, jsonBody);
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    // ---------------------------
    // Utils
    // ---------------------------
    private static Integer parseInt(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static String nvl(String s, String d) { return s == null ? d : s; }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return m == null ? "" : m.replace("\n", " ").replace("\r", " ").trim();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(nvl(s), StandardCharsets.UTF_8);
    }

    private static String redirect(HttpServletRequest request, String relative) {
        // garante que funciona mesmo quando o projeto não está na raiz (/)
        String ctx = request.getContextPath();
        if (ctx == null) ctx = "";
        if (!relative.startsWith("/")) relative = "/" + relative;
        return ctx + relative;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("</", "<\\/");
    }
}
