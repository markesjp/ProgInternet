package autoescola.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.dao.AlunoDao;
import autoescola.model.Aluno;

/**
 * BuscaAlunoServlet
 * - Processa busca (CPF/Nome/Status) e encaminha para JSP distintas via jsp:forward (roteadora)
 */
@WebServlet("/buscar-aluno")
public class BuscaAlunoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AlunoDao alunoDao;

    @Override
    public void init() throws ServletException {
        super.init();
        this.alunoDao = new AlunoDao();
        getServletContext().log("[BuscaAlunoServlet] init()");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long t0 = System.currentTimeMillis();
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            super.service(req, resp);
        } finally {
            getServletContext().log("[BuscaAlunoServlet] " + req.getMethod() + " " + req.getRequestURI()
                    + " " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/busca/busca.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cpf = nvl(req.getParameter("cpf")).trim();
        String nome = nvl(req.getParameter("nome")).trim();
        String status = nvl(req.getParameter("status")).trim().toUpperCase();

        String cpfDigits = cpf.replaceAll("\\D", "");

        if (cpfDigits.isEmpty() && nome.isEmpty()) {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Informe CPF e/ou Nome para realizar a busca.");
            req.getRequestDispatcher("/WEB-INF/jsp/busca/busca_resultado.jsp").forward(req, resp);
            return;
        }

        if (!cpfDigits.isEmpty() && cpfDigits.length() != 11) {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "CPF incompleto. Informe os 11 dígitos.");
            req.getRequestDispatcher("/WEB-INF/jsp/busca/busca_resultado.jsp").forward(req, resp);
            return;
        }

        try {
            List<Aluno> resultados = new ArrayList<>();

            if (!cpfDigits.isEmpty()) {
                Aluno a = alunoDao.buscarPorCpf(cpfDigits, status);
                if (a != null) resultados.add(a);
            } else {
                resultados = alunoDao.listarPorNome(nome, status);
            }

            if (resultados == null || resultados.isEmpty()) {
                req.setAttribute("resultado", "erro");
                req.setAttribute("erro", "Nenhum aluno encontrado para os filtros informados.");
            } else {
                req.setAttribute("resultado", "sucesso");
                req.setAttribute("alunos", resultados);
            }

            req.getRequestDispatcher("/WEB-INF/jsp/busca/busca_resultado.jsp").forward(req, resp);
        } catch (Exception e) {
            getServletContext().log("[BuscaAlunoServlet] erro", e);
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Erro interno ao consultar o banco de dados.");
            req.getRequestDispatcher("/WEB-INF/jsp/busca/busca_resultado.jsp").forward(req, resp);
        }
    }

    @Override
    public void destroy() {
        getServletContext().log("[BuscaAlunoServlet] destroy()");
        this.alunoDao = null;
        super.destroy();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
