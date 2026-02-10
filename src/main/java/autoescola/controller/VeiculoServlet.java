package autoescola.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.dao.VeiculoDao;
import autoescola.model.Veiculo;

/**
 * VeiculoServlet
 *
 * Melhorias:
 * - API JSON para interface moderna (tabela + edição + exclusão).
 * - Validações e tratamento de erros.
 * - Ciclo de vida: init(), service(), destroy().
 */
@WebServlet("/veiculos")
public class VeiculoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private VeiculoDao dao;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dao = new VeiculoDao();
        getServletContext().log("[VeiculoServlet] init() - inicializado");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            super.service(req, resp);
        } finally {
            long ms = System.currentTimeMillis() - start;
            getServletContext().log("[VeiculoServlet] service() - " + req.getMethod() + " " + req.getRequestURI() + " (" + ms + "ms)");
        }
    }

    @Override
    public void destroy() {
        getServletContext().log("[VeiculoServlet] destroy() - finalizando");
        this.dao = null;
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isJsonRequest(request)) {
            request.getRequestDispatcher("/WEB-INF/jsp/pages/veiculos.jsp").forward(request, response);
        return;
}

        String op = nvl(request.getParameter("op"), "list");

        try {
            if ("list".equalsIgnoreCase(op)) {
                List<Veiculo> veiculos = dao.listar();
                writeJson(response, 200, veiculosToJson(veiculos));
                return;
            }

            if ("get".equalsIgnoreCase(op)) {
                Integer id = parseInt(request.getParameter("id"));
                if (id == null || id <= 0) {
                    writeJson(response, 400, errJson("Parâmetro 'id' inválido."));
                    return;
                }
                Veiculo v = dao.buscarPorId(id);
                if (v == null) {
                    writeJson(response, 404, errJson("Veículo não encontrado."));
                    return;
                }
                writeJson(response, 200, veiculoToJson(v));
                return;
            }

            writeJson(response, 400, errJson("Operação inválida: op=" + op));
        } catch (RuntimeException e) {
            writeJson(response, 500, errJson("Falha ao consultar veículos: " + safeMsg(e)));
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
                Veiculo v = readVeiculoFromRequest(request, false);
                validateVeiculo(v, false);

                if (dao.existePlaca(v.getPlaca())) {
                    respond(response, json, 409, errJson("Placa já cadastrada."), "veiculos?erro=Placa+já+cadastrada");
                    return;
                }

                dao.inserir(v);
                respond(response, json, 200, okJson("Veículo cadastrado com sucesso."), "veiculos?msg=Veículo+cadastrado");
                return;
            }

            if ("update".equals(op)) {
                Veiculo v = readVeiculoFromRequest(request, true);
                validateVeiculo(v, true);

                Veiculo existente = dao.buscarPorId(v.getId());
                if (existente == null) {
                    respond(response, json, 404, errJson("Veículo não encontrado."), "veiculos?erro=Veículo+não+encontrado");
                    return;
                }

                if (!safeEquals(existente.getPlaca(), v.getPlaca()) && dao.existePlaca(v.getPlaca())) {
                    respond(response, json, 409, errJson("Placa já cadastrada."), "veiculos?erro=Placa+já+cadastrada");
                    return;
                }

                dao.alterar(v);
                respond(response, json, 200, okJson("Veículo atualizado com sucesso."), "veiculos?msg=Veículo+atualizado");
                return;
            }

            if ("delete".equals(op)) {
                Integer id = parseInt(nvl(request.getParameter("id"), request.getParameter("id_remover")));
                if (id == null || id <= 0) {
                    respond(response, json, 400, errJson("ID inválido."), "veiculos?erro=ID+inválido");
                    return;
                }
                Veiculo existente = dao.buscarPorId(id);
                if (existente == null) {
                    respond(response, json, 404, errJson("Veículo não encontrado."), "veiculos?erro=Veículo+não+encontrado");
                    return;
                }
                dao.remover(id);
                respond(response, json, 200, okJson("Veículo removido com sucesso."), "veiculos?msg=Veículo+removido");
                return;
            }

            respond(response, json, 400, errJson("Operação inválida."), "veiculos?erro=Operação+inválida");
        } catch (IllegalArgumentException e) {
            respond(response, json, 400, errJson(safeMsg(e)), "veiculos?erro=" + urlEncode(safeMsg(e)));
        } catch (RuntimeException e) {
            respond(response, json, 500, errJson("Falha ao processar: " + safeMsg(e)), "veiculos?erro=Falha+ao+processar");
        }
    }

    private Veiculo readVeiculoFromRequest(HttpServletRequest request, boolean requireId) {
        Veiculo v = new Veiculo();

        if (requireId) {
            Integer id = parseInt(request.getParameter("id"));
            if (id == null || id <= 0) throw new IllegalArgumentException("Informe um ID válido para editar.");
            v.setId(id);
        }

        String placa = trim(request.getParameter("placa")).toUpperCase();
        v.setPlaca(placa);
        v.setModelo(trim(request.getParameter("modelo")));
        v.setMarca(trim(request.getParameter("marca")));
        v.setCategoria(trim(request.getParameter("categoria")));
        v.setStatus(trim(request.getParameter("status")));

        Integer ano = parseInt(request.getParameter("ano"));
        if (ano != null) v.setAno(ano);

        return v;
    }

    private void validateVeiculo(Veiculo v, boolean requireId) {
        if (requireId && (v.getId() == null || v.getId() <= 0)) throw new IllegalArgumentException("ID inválido.");
        if (isBlank(v.getPlaca()) || v.getPlaca().length() < 7) throw new IllegalArgumentException("Placa é obrigatória.");
        if (isBlank(v.getModelo())) throw new IllegalArgumentException("Modelo é obrigatório.");
        if (isBlank(v.getMarca())) throw new IllegalArgumentException("Marca é obrigatória.");
        if (v.getAno() <= 0) throw new IllegalArgumentException("Ano inválido.");
        if (isBlank(v.getCategoria())) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (isBlank(v.getStatus())) throw new IllegalArgumentException("Status é obrigatório.");
    }

    private String veiculosToJson(List<Veiculo> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int x = 0; x < list.size(); x++) {
            if (x > 0) sb.append(",");
            sb.append(veiculoToJson(list.get(x)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String veiculoToJson(Veiculo v) {
        return "{" +
                "\"id\":" + v.getId() + "," +
                "\"placa\":\"" + j(v.getPlaca()) + "\"," +
                "\"modelo\":\"" + j(v.getModelo()) + "\"," +
                "\"marca\":\"" + j(v.getMarca()) + "\"," +
                "\"ano\":" + v.getAno() + "," +
                "\"categoria\":\"" + j(v.getCategoria()) + "\"," +
                "\"status\":\"" + j(v.getStatus()) + "\"" +
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
