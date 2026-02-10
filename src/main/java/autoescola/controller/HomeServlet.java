package autoescola.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HomeServlet
 * - Página inicial em JSP (Opção 2).
 * - Mostra como separar VIEW (JSP) e API (JSON) no mesmo projeto.
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().log("[HomeServlet] init()");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long t0 = System.currentTimeMillis();
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            super.service(req, resp);
        } finally {
            getServletContext().log("[HomeServlet] " + req.getMethod() + " " + req.getRequestURI()
                    + " " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/pages/home.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        getServletContext().log("[HomeServlet] destroy()");
        super.destroy();
    }
}
