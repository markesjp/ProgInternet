package autoescola.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

/**
 * DownloadServlet (Atividade)
 *
 * Permite baixar os 2 arquivos enviados pelo usuário, respeitando o "contexto" (sessão).
 *
 * Endpoint: /download?slot=1 ou /download?slot=2
 */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Path uploadsBaseDir;

    @Override
    public void init() throws ServletException {
        super.init();

        String real = getServletContext().getRealPath("/WEB-INF/uploads");
        if (real != null) {
            uploadsBaseDir = Paths.get(real);
        } else {
            uploadsBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_uploads");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Faça login para baixar arquivos.");
            return;
        }

        String slot = req.getParameter("slot");
        if (!"1".equals(slot) && !"2".equals(slot)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetro 'slot' inválido.");
            return;
        }

        Map<String, String> files = (Map<String, String>) session.getAttribute(UploadServlet.SESSION_FILES_KEY);
        if (files == null || files.get(slot) == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Nenhum arquivo encontrado para este slot.");
            return;
        }

        Path file = Paths.get(files.get(slot)).normalize().toAbsolutePath();
        Path base = uploadsBaseDir.normalize().toAbsolutePath();

        // Proteção contra path traversal / download fora da pasta de uploads.
        if (!file.startsWith(base) || !Files.exists(file) || Files.isDirectory(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Arquivo não encontrado.");
            return;
        }

        String fileName = file.getFileName().toString();
        String contentType = getServletContext().getMimeType(fileName);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.replace("\"", "") + "\"");
        resp.setHeader("Content-Length", String.valueOf(Files.size(file)));

        try (InputStream in = Files.newInputStream(file)) {
            IOUtils.copy(in, resp.getOutputStream());
        }
    }
}
