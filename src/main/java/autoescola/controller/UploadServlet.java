package autoescola.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.javax.JavaxServletDiskFileUpload;
import org.apache.commons.fileupload2.javax.JavaxServletFileUpload;
import org.apache.commons.io.FilenameUtils;

@WebServlet("/upload")
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String SESSION_FILES_KEY_V2 = "uploadedFilesV2";

    private Path tempRepository;
    private Path uploadsBaseDir;
    private Path uploadsPublicDir;

    @Override
    public void init() throws ServletException {
        super.init();

        File containerTempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
        if (containerTempDir != null) {
            tempRepository = containerTempDir.toPath();
        } else {
            tempRepository = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_upload_tmp");
        }

        String real = getServletContext().getRealPath("/WEB-INF/uploads");
        if (real != null) {
            uploadsBaseDir = Paths.get(real);
        } else {
            uploadsBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_uploads");
        }

        uploadsPublicDir = uploadsBaseDir.resolve("public");

        try {
            Files.createDirectories(tempRepository);
            Files.createDirectories(uploadsBaseDir);
            Files.createDirectories(uploadsPublicDir);
        } catch (IOException e) {
            throw new ServletException("Falha ao criar diretórios de upload.", e);
        }

        getServletContext().log("[UploadServlet] tempRepository=" + tempRepository);
        getServletContext().log("[UploadServlet] uploadsBaseDir=" + uploadsBaseDir);
        getServletContext().log("[UploadServlet] uploadsPublicDir=" + uploadsPublicDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);

        @SuppressWarnings("unchecked")
        Map<String, DocInfo> sessionFiles =
                (Map<String, DocInfo>) session.getAttribute(SESSION_FILES_KEY_V2);

        if (sessionFiles == null) sessionFiles = new LinkedHashMap<>();

        req.setAttribute("sessionFiles", sessionFiles);
        req.getRequestDispatcher("/WEB-INF/jsp/pages/upload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);

        if (!JavaxServletFileUpload.isMultipartContent(req)) {
            resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Requisição inválida: esperado multipart/form-data."));
            return;
        }

        DiskFileItemFactory factory = DiskFileItemFactory.builder()
                .setPath(tempRepository)
                .get();

        JavaxServletDiskFileUpload upload = new JavaxServletDiskFileUpload(factory);
        upload.setHeaderCharset(java.nio.charset.StandardCharsets.UTF_8);

        try {
            List<DiskFileItem> items = upload.parseRequest(req);

            String categoria = null;
            List<DiskFileItem> filesToSave = new ArrayList<>();

            for (DiskFileItem item : items) {
                if (item == null) continue;

                if (item.isFormField()) {
                    if ("categoria".equals(item.getFieldName())) {
                        categoria = item.getString(java.nio.charset.StandardCharsets.UTF_8);
                    }
                    continue;
                }

                if ("arquivos".equals(item.getFieldName()) && item.getSize() > 0) {
                    filesToSave.add(item);
                }
            }

            if (categoria == null || categoria.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Selecione a categoria do comprovante."));
                return;
            }

            if (filesToSave.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Selecione pelo menos 1 arquivo para enviar."));
                return;
            }

            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            @SuppressWarnings("unchecked")
            Map<String, DocInfo> sessionFiles =
                    (Map<String, DocInfo>) session.getAttribute(SESSION_FILES_KEY_V2);
            if (sessionFiles == null) sessionFiles = new LinkedHashMap<>();

            int okCount = 0;

            for (DiskFileItem f : filesToSave) {
                String id = UUID.randomUUID().toString();

                String original = safeOriginalName(f.getName());
                String stored = ts + "_" + id + "_" + original;

                Path dest = uploadsPublicDir.resolve(stored).normalize().toAbsolutePath();
                Path base = uploadsPublicDir.normalize().toAbsolutePath();

                if (!dest.startsWith(base)) {
                    getServletContext().log("[UploadServlet] tentativa de path traversal: " + dest);
                    continue;
                }

                // garante pasta
                Files.createDirectories(uploadsPublicDir);

                // grava
                f.write(dest);

                // sanity check
                if (!Files.exists(dest) || !Files.isRegularFile(dest)) {
                    getServletContext().log("[UploadServlet] arquivo não foi criado após write(): " + dest);
                    continue;
                }

                DocInfo info = new DocInfo();
                info.id = id;
                info.originalName = original;
                info.storedName = stored;
                info.contentType = (f.getContentType() != null ? f.getContentType() : "application/octet-stream");
                info.sizeBytes = f.getSize();
                info.uploadedAtEpoch = System.currentTimeMillis();
                info.categoria = categoria;

                sessionFiles.put(id, info);
                okCount++;
            }

            session.setAttribute(SESSION_FILES_KEY_V2, sessionFiles);

            if (okCount == 0) {
                resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Nenhum arquivo pôde ser salvo."));
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/upload?msg=" + url(okCount + " arquivo(s) enviado(s) com sucesso."));
        } catch (FileUploadException e) {
            getServletContext().log("[UploadServlet] FileUploadException", e);
            resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Falha ao processar upload: " + e.getMessage()));
        } catch (Exception e) {
            getServletContext().log("[UploadServlet] Exception", e);
            resp.sendRedirect(req.getContextPath() + "/upload?erro=" + url("Erro inesperado no upload: " + e.getMessage()));
        }
    }

    private static String safeOriginalName(String original) {
        String base = FilenameUtils.getName(original == null ? "" : original);
        if (base.isBlank()) base = "arquivo.bin";
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String url(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }

    public static class DocInfo {
        public String id;
        public String originalName;
        public String storedName;
        public String contentType;
        public long sizeBytes;
        public long uploadedAtEpoch;
        public String categoria;
    }
}
