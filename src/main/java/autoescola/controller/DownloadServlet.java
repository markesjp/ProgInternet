package autoescola.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;

/**
 * DownloadServlet (sem banco, por sessão + disco)
 *
 * Endpoint: /download?id=<uuid>
 *
 * - Exige login (mantém coerência com seu modelo de sessão)
 * - Procura o DocInfo no Map da sessão "uploadedFilesV2"
 * - Faz download do arquivo salvo em WEB-INF/uploads/<usuario>/<storedName>
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

        try {
            Files.createDirectories(uploadsBaseDir);
        } catch (IOException e) {
            throw new ServletException("Falha ao criar diretório base de uploads.", e);
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

        String usuarioLogado = (String) session.getAttribute("usuarioLogado");
        String safeUser = sanitizeUser(usuarioLogado);

        String id = req.getParameter("id");
        if (id == null || id.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetro 'id' é obrigatório.");
            return;
        }

        Map<String, UploadServlet.DocInfo> files =
                (Map<String, UploadServlet.DocInfo>) session.getAttribute(UploadServlet.SESSION_FILES_KEY_V2);

        UploadServlet.DocInfo info = (files == null) ? null : files.get(id);
        if (info == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Arquivo não encontrado na sessão.");
            return;
        }

        Path base = uploadsBaseDir.resolve(safeUser).normalize().toAbsolutePath();
        Path file = base.resolve(info.storedName).normalize().toAbsolutePath();

        // Proteção: download só dentro do diretório do usuário
        if (!file.startsWith(base) || !Files.exists(file) || Files.isDirectory(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Arquivo não encontrado.");
            return;
        }

        String fileName = (info.originalName != null && !info.originalName.isBlank())
                ? info.originalName
                : file.getFileName().toString();

        String contentType = info.contentType;
        if (contentType == null || contentType.isBlank()) {
            contentType = getServletContext().getMimeType(fileName);
        }
        if (contentType == null) contentType = "application/octet-stream";

        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.replace("\"", "") + "\"");
        resp.setHeader("Content-Length", String.valueOf(Files.size(file)));

        try (InputStream in = Files.newInputStream(file)) {
            IOUtils.copy(in, resp.getOutputStream());
        }
    }

    private static String sanitizeUser(String user) {
        return user.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
