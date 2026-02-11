package autoescola.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public final class DocumentosStore {

    private DocumentosStore() {}

    // Pastas/arquivos dentro do tempdir do container (ou do servidor)
    private static final String BASE_DIR_NAME = "autoescola_uploads";
    private static final Pattern SAFE_USER = Pattern.compile("[^a-zA-Z0-9._-]");

    public static final class DocInfo {
        public final int slot;
        public final String originalName;
        public final long sizeBytes;
        public final Instant lastModified;

        public DocInfo(int slot, String originalName, long sizeBytes, Instant lastModified) {
            this.slot = slot;
            this.originalName = originalName;
            this.sizeBytes = sizeBytes;
            this.lastModified = lastModified;
        }
    }

    /**
     * Define uma chave de "usuário" para o contexto de documentos.
     * - Se houver login, você pode setar uma chave fixa (ex: usuário).
     * - Caso contrário, cai no session id.
     */
    public static String resolveUserKey(HttpServletRequest req) {
        Object key = req.getSession().getAttribute("docs_user_key");
        if (key != null && !String.valueOf(key).isBlank()) return String.valueOf(key);
        return "sess-" + req.getSession().getId();
    }

    public static void setUserKey(HttpServletRequest req, String userKey) {
        req.getSession().setAttribute("docs_user_key", sanitizeUserKey(userKey));
    }

    public static String sanitizeUserKey(String raw) {
        if (raw == null || raw.isBlank()) return "anon";
        return SAFE_USER.matcher(raw.trim()).replaceAll("_");
    }

    private static Path baseDir(ServletContext ctx) throws IOException {
        Object tmpObj = ctx.getAttribute("javax.servlet.context.tempdir");
        Path tempDir = (tmpObj instanceof File) ? ((File) tmpObj).toPath() : Path.of(System.getProperty("java.io.tmpdir"));
        Path base = tempDir.resolve(BASE_DIR_NAME);
        Files.createDirectories(base);
        return base;
    }

    private static Path userDir(ServletContext ctx, String userKey) throws IOException {
        Path base = baseDir(ctx);
        Path udir = base.resolve(sanitizeUserKey(userKey));
        Files.createDirectories(udir);
        return udir;
    }

    private static Path slotFile(ServletContext ctx, String userKey, int slot) throws IOException {
        return userDir(ctx, userKey).resolve("doc" + slot + ".bin");
    }

    private static Path slotMeta(ServletContext ctx, String userKey, int slot) throws IOException {
        return userDir(ctx, userKey).resolve("doc" + slot + ".meta");
    }

    /** Usa commons-io para extrair apenas o "nome" do arquivo e remove caracteres problemáticos. */
    public static String safeFileName(String name) {
        if (name == null || name.isBlank()) return "arquivo";

        // remove diretórios caso o browser envie caminho (Windows, etc.)
        String base = FilenameUtils.getName(name).trim();
        if (base.isBlank()) base = "arquivo";

        // tira caracteres perigosos
        base = base.replaceAll("[\\r\\n\\t\\0]", "");
        base = base.replaceAll("[\\\\/]", "_");
        base = base.replaceAll("[:*?\"<>|]", "_");

        // limita comprimento
        if (base.length() > 120) base = base.substring(base.length() - 120);
        return base;
    }

    public static void saveToSlot(ServletContext ctx, String userKey, int slot, InputStream data, String originalName) throws IOException {
        String safeName = safeFileName(originalName);
        Path bin = slotFile(ctx, userKey, slot);
        Path meta = slotMeta(ctx, userKey, slot);

        Files.createDirectories(bin.getParent());
        Files.copy(data, bin, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        try (BufferedWriter w = Files.newBufferedWriter(meta, StandardCharsets.UTF_8)) {
            w.write(safeName);
            w.newLine();
        }
    }

    public static Optional<DocInfo> getDocInfo(ServletContext ctx, String userKey, int slot) throws IOException {
        Path bin = slotFile(ctx, userKey, slot);
        if (!Files.exists(bin)) return Optional.empty();

        Path meta = slotMeta(ctx, userKey, slot);
        String name = "documento-" + slot;
        if (Files.exists(meta)) {
            try (BufferedReader r = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
                String line = r.readLine();
                if (line != null && !line.isBlank()) name = line.trim();
            }
        }

        long size = Files.size(bin);
        Instant lm = Instant.ofEpochMilli(Files.getLastModifiedTime(bin).toMillis());
        return Optional.of(new DocInfo(slot, name, size, lm));
    }

    public static void streamSlotTo(ServletContext ctx, String userKey, int slot, OutputStream out) throws IOException {
        Path bin = slotFile(ctx, userKey, slot);
        try (InputStream in = Files.newInputStream(bin)) {
            IOUtils.copy(in, out);
        }
    }
}
