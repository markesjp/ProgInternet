package autoescola.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LoginServlet (Atividade)
 * - Demonstra o ciclo de vida (init/service/destroy)
 * - Processa requisição e encaminha para páginas JSP distintas usando jsp:forward
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().log("[LoginServlet] init()");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long t0 = System.currentTimeMillis();
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            super.service(req, resp);
        } finally {
            getServletContext().log("[LoginServlet] " + req.getMethod() + " " + req.getRequestURI()
                    + " " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = nvl(req.getParameter("usuario")).trim();
        String senha = nvl(req.getParameter("senha")).trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Informe usuário e senha.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login_resultado.jsp").forward(req, resp);
            return;
        }

        // Exemplo simples (atividade). Se quiser, dá para validar no banco.
        boolean ok = "admin".equalsIgnoreCase(usuario) && "admin".equals(senha);

        if (ok) {
            req.setAttribute("resultado", "sucesso");
            req.setAttribute("usuario", usuario);

            // Mantém o usuário logado na sessão (usado pelo módulo de upload).
            req.getSession(true).setAttribute("usuarioLogado", usuario);
        } else {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Usuário ou senha inválidos.");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/auth/login_resultado.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        getServletContext().log("[LoginServlet] destroy()");
        super.destroy();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
