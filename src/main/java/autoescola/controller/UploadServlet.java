package autoescola.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.javax.JavaxServletDiskFileUpload;
import org.apache.commons.fileupload2.javax.JavaxServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 * UploadServlet (Atividade)
 *
 * Requisito:
 * - Qualquer usuário deve conseguir fazer upload de 2 arquivos e (opcionalmente) baixá-los no seu contexto.
 * - Implementação preparada para usar commons-fileupload2 + commons-io (basta colocar os JARs em WEB-INF/lib).
 */
@WebServlet("/upload")
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Chaves de sessão para armazenar os caminhos dos uploads do usuário.
    public static final String SESSION_FILES_KEY = "uploadedFiles";

    // Pasta segura para temporários do FileUpload (evita usar java.io.tmpdir).
    private Path tempRepository;

    // Pasta onde efetivamente gravamos os arquivos (por usuário).
    private Path uploadsBaseDir;

    @Override
    public void init() throws ServletException {
        super.init();

        // Repositório temporário recomendado pela própria lib.
        File containerTempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
        if (containerTempDir != null) {
            tempRepository = containerTempDir.toPath();
        } else {
            tempRepository = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_upload_tmp");
        }

        // Pasta interna do app: WEB-INF/uploads (fallback para temp se getRealPath() vier null).
        String real = getServletContext().getRealPath("/WEB-INF/uploads");
        if (real != null) {
            uploadsBaseDir = Paths.get(real);
        } else {
            uploadsBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "autoescola_uploads");
        }

        try {
            Files.createDirectories(tempRepository);
            Files.createDirectories(uploadsBaseDir);
        } catch (IOException e) {
            throw new ServletException("Falha ao criar diretórios de upload.", e);
        }

        getServletContext().log("[UploadServlet] tempRepository=" + tempRepository);
        getServletContext().log("[UploadServlet] uploadsBaseDir=" + uploadsBaseDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/upload/upload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);
        String usuarioLogado = (String) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.isBlank()) {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Você precisa estar logado para enviar arquivos (faça login e tente novamente).");
            req.getRequestDispatcher("/WEB-INF/jsp/upload/upload_resultado.jsp").forward(req, resp);
            return;
        }

        if (!JavaxServletFileUpload.isMultipartContent(req)) {
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Requisição inválida: esperado multipart/form-data.");
            req.getRequestDispatcher("/WEB-INF/jsp/upload/upload_resultado.jsp").forward(req, resp);
            return;
        }

        // Factory para itens em disco (config segura do path temporário).
        DiskFileItemFactory factory = DiskFileItemFactory.builder()
                .setPath(tempRepository)
                .get();

        JavaxServletDiskFileUpload upload = new JavaxServletDiskFileUpload(factory);
        upload.setHeaderCharset(java.nio.charset.StandardCharsets.UTF_8);

        try {
            List<DiskFileItem> items = upload.parseRequest(req);

            DiskFileItem f1 = null;
            DiskFileItem f2 = null;

            for (DiskFileItem item : items) {
                if (item == null || item.isFormField()) {
                    continue;
                }

                String field = item.getFieldName();
                if ("arquivo1".equals(field)) {
                    f1 = item;
                } else if ("arquivo2".equals(field)) {
                    f2 = item;
                }
            }

            if (f1 == null || f1.getSize() <= 0 || f2 == null || f2.getSize() <= 0) {
                req.setAttribute("resultado", "erro");
                req.setAttribute("erro", "Envie os 2 arquivos obrigatórios (Arquivo 1 e Arquivo 2).");
                req.getRequestDispatcher("/WEB-INF/jsp/upload/upload_resultado.jsp").forward(req, resp);
                return;
            }

            String safeUser = sanitizeUser(usuarioLogado);
            Path userDir = uploadsBaseDir.resolve(safeUser);
            Files.createDirectories(userDir);

            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String nome1 = safeOriginalName(f1.getName());
            String nome2 = safeOriginalName(f2.getName());

            Path dest1 = userDir.resolve("1_" + ts + "_" + nome1);
            Path dest2 = userDir.resolve("2_" + ts + "_" + nome2);

            f1.write(dest1);
            f2.write(dest2);

            // Salva na sessão os caminhos para download (por usuário/ sessão).
            Map<String, String> files = new HashMap<>();
            files.put("1", dest1.toString());
            files.put("2", dest2.toString());
            session.setAttribute(SESSION_FILES_KEY, files);

            req.setAttribute("resultado", "sucesso");
            req.setAttribute("arquivo1Nome", nome1);
            req.setAttribute("arquivo2Nome", nome2);

            req.getRequestDispatcher("/WEB-INF/jsp/upload/upload_resultado.jsp").forward(req, resp);

        } catch (FileUploadException e) {
            getServletContext().log("[UploadServlet] FileUploadException", e);
            req.setAttribute("resultado", "erro");
            req.setAttribute("erro", "Falha ao processar upload: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/upload/upload_resultado.jsp").forward(req, resp);
        }
    }

    private static String sanitizeUser(String user) {
        // Mantém somente caracteres seguros para nome de pasta.
        return user.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String safeOriginalName(String original) {
        // Remove path (Windows/Unix) e normaliza.
        String base = FilenameUtils.getName(original == null ? "" : original);
        if (base.isBlank()) {
            base = "arquivo.bin";
        }
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
