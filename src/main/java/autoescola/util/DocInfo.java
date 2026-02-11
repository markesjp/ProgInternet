package autoescola.util;

public class DocInfo {
    public String id;            // uuid
    public String originalName;
    public String storedName;    // nome no disco
    public String contentType;
    public long sizeBytes;
    public long uploadedAtEpoch; // System.currentTimeMillis()
    public String categoria;     // PAGAMENTO/GASOLINA/OUTROS (opcional)
}
