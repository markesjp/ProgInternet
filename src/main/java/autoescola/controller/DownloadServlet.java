package autoescola.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Path uploadsBaseDir;

    @Override
    public void init() throws ServletException {
        super.init();
        String real = getServletContext().getRealPath("/WEB-INF/uploads");
        if (real != null) uploadsBaseDir = Paths.get(real);
        else uploadsBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_uploads");

        getServletContext().log("[DownloadServlet] uploadsBaseDir=" + uploadsBaseDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            redirectErro(req, resp, "Sessão expirada. Faça upload novamente.");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, UploadServlet.DocInfo> sessionFiles =
                (Map<String, UploadServlet.DocInfo>) session.getAttribute(UploadServlet.SESSION_FILES_KEY_V2);

        if (sessionFiles == null || sessionFiles.isEmpty()) {
            redirectErro(req, resp, "Nenhum arquivo disponível para download nesta sessão.");
            return;
        }

        // 1) suporte a 1 arquivo via ?id=...
        String id = req.getParameter("id");
        if (id != null && !id.isBlank()) {
            UploadServlet.DocInfo info = sessionFiles.get(id);
            if (info == null) {
                redirectErro(req, resp, "Arquivo não encontrado na sessão.");
                return;
            }
            streamSingle(req, resp, info);
            return;
        }

        // 2) suporte a múltiplos via ?ids=1&ids=2 (checkbox)
        String[] ids = req.getParameterValues("ids");
        if (ids == null || ids.length == 0) {
            redirectErro(req, resp, "Selecione ao menos 1 arquivo para baixar.");
            return;
        }

        List<UploadServlet.DocInfo> selected = new ArrayList<>();
        for (String x : ids) {
            if (x == null || x.isBlank()) continue;
            UploadServlet.DocInfo info = sessionFiles.get(x.trim());
            if (info != null) selected.add(info);
        }

        if (selected.isEmpty()) {
            redirectErro(req, resp, "Seleção inválida. Nenhum arquivo encontrado para os IDs informados.");
            return;
        }

        if (selected.size() == 1) {
            streamSingle(req, resp, selected.get(0));
            return;
        }

        streamZip(req, resp, selected);
    }

    // =========================
    // Download 1 arquivo
    // =========================
    private void streamSingle(HttpServletRequest req, HttpServletResponse resp, UploadServlet.DocInfo info) throws IOException {
        Path base = uploadsBaseDir.resolve("public").normalize().toAbsolutePath();
        Path file = base.resolve(info.storedName).normalize().toAbsolutePath();

        if (!file.startsWith(base)) {
            resp.sendError(400, "Caminho inválido.");
            return;
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            redirectErro(req, resp, "Arquivo não existe mais no disco.");
            return;
        }

        String contentType = info.contentType;
        if (contentType == null || contentType.isBlank()) {
            contentType = Files.probeContentType(file);
            if (contentType == null) contentType = "application/octet-stream";
        }

        resp.setContentType(contentType);
        resp.setHeader("X-Content-Type-Options", "nosniff");

        String downloadName = safeDownloadName(info.originalName);
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + downloadName + "\"; filename*=UTF-8''" + encodeRFC5987(downloadName));

        long size = Files.size(file);
        if (size >= 0) resp.setContentLengthLong(size);

        try (InputStream in = Files.newInputStream(file)) {
            in.transferTo(resp.getOutputStream());
        }
    }

    // =========================
    // Download ZIP (vários)
    // =========================
    private void streamZip(HttpServletRequest req, HttpServletResponse resp, List<UploadServlet.DocInfo> files) throws IOException {
        Path base = uploadsBaseDir.resolve("public").normalize().toAbsolutePath();

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String zipName = "comprovantes_" + ts + ".zip";

        resp.setContentType("application/zip");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + zipName + "\"; filename*=UTF-8''" + encodeRFC5987(zipName));

        // para não explodir o ZIP com nomes repetidos
        Set<String> usedNames = new HashSet<>();

        try (ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream())) {
            zos.setLevel(6);

            for (UploadServlet.DocInfo info : files) {
                Path file = base.resolve(info.storedName).normalize().toAbsolutePath();
                if (!file.startsWith(base)) continue;
                if (!Files.exists(file) || !Files.isRegularFile(file)) continue;

                String entryName = safeZipEntryName(info.originalName);
                entryName = uniqueName(entryName, usedNames);

                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);

                try (InputStream in = Files.newInputStream(file)) {
                    in.transferTo(zos);
                }

                zos.closeEntry();
            }
            zos.finish();
        }
    }

    // =========================
    // Helpers
    // =========================
    private void redirectErro(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url(msg));
    }

    private static String safeDownloadName(String name) {
        if (name == null || name.isBlank()) return "arquivo.bin";
        return name.replace("\"", "").replace("\r", "").replace("\n", "");
    }

    private static String safeZipEntryName(String name) {
        // Evita path dentro do zip (../ etc)
        if (name == null || name.isBlank()) return "arquivo.bin";
        String n = name.replace("\\", "/");
        int lastSlash = n.lastIndexOf('/');
        if (lastSlash >= 0) n = n.substring(lastSlash + 1);
        n = n.replace("\r", "").replace("\n", "");
        if (n.isBlank()) n = "arquivo.bin";
        return n;
    }

    private static String uniqueName(String name, Set<String> used) {
        if (used.add(name)) return name;

        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            base = name.substring(0, dot);
            ext = name.substring(dot);
        }

        int i = 2;
        while (true) {
            String candidate = base + " (" + i + ")" + ext;
            if (used.add(candidate)) return candidate;
            i++;
        }
    }

    private static String url(String s) {
        try { return URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }

    private static String encodeRFC5987(String s) {
        try { return URLEncoder.encode(s, "UTF-8").replace("+", "%20"); } catch (Exception e) { return s; }
    }
}
