package autoescola.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autoescola.util.DocumentosStore;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Login simples (exercício): user=admin senha=123 ou user=aluno senha=123
    private boolean validar(String usuario, String senha) {
        if (usuario == null || senha == null) return false;
        usuario = usuario.trim();
        senha = senha.trim();
        return ("admin".equalsIgnoreCase(usuario) && "123".equals(senha))
            || ("aluno".equalsIgnoreCase(usuario) && "123".equals(senha));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getParameter("usuario");
        String senha = req.getParameter("senha");

        boolean ok = validar(usuario, senha);

        if (ok) {
            req.setAttribute("resultado", "sucesso");
            req.setAttribute("usuario", usuario);

            // Contexto de documentos: após login, fixa o "usuário" para não depender do session id
            DocumentosStore.setUserKey(req, usuario);
        } else {
            req.setAttribute("resultado", "erro");
            req.setAttribute("mensagem", "Usuário ou senha inválidos.");
        }

        // JSP com <jsp:forward> encaminha para sucesso/erro
        req.getRequestDispatcher("/WEB-INF/jsp/auth/login_resultado.jsp").forward(req, resp);
    }
}
