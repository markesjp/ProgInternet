package autoescola.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.util.DocumentosStore;

@WebServlet(urlPatterns = {"/home", "/"})
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Documentos (2 slots por usuário/sessão)
        String userKey = DocumentosStore.resolveUserKey(req);

        req.setAttribute("docs_user_key", userKey);
        req.setAttribute("doc1", DocumentosStore.getDocInfo(getServletContext(), userKey, 1).orElse(null));
        req.setAttribute("doc2", DocumentosStore.getDocInfo(getServletContext(), userKey, 2).orElse(null));

        req.getRequestDispatcher("/WEB-INF/jsp/pages/home.jsp").forward(req, resp);
    }
}
