package autoescola.model;

import java.time.LocalDateTime;

// Representa uma aula agendada no sistema.
// Ela conecta um aluno, um instrutor e um veículo.
public class Aula {
    private Integer id;
    private Integer alunoId; // Chave estrangeira para Aluno
    private Integer instrutorId; // Chave estrangeira para Instrutor
    private Integer veiculoId; // Chave estrangeira para Veiculo
    private LocalDateTime dataAula;
    private Integer duracaoMinutos;
    private String tipo; // "PRATICA", "TEORICA"
    private String status; // "AGENDADA", "CONCLUIDA", "CANCELADA"
    private String observacoes;

    // Construtor vazio.
    public Aula() {}

    // Construtor para agendar uma nova aula.
    public Aula(Integer alunoId, Integer instrutorId, Integer veiculoId,
                LocalDateTime dataAula, Integer duracaoMinutos, String tipo,
                String status, String observacoes) {
        this.alunoId = alunoId;
        this.instrutorId = instrutorId;
        this.veiculoId = veiculoId;
        this.dataAula = dataAula;
        this.duracaoMinutos = duracaoMinutos;
        this.tipo = tipo;
        this.status = status;
        this.observacoes = observacoes;
    }

    // Getters e Setters.
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }

    public Integer getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Integer instrutorId) { this.instrutorId = instrutorId; }

    public Integer getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Integer veiculoId) { this.veiculoId = veiculoId; }

    public LocalDateTime getDataAula() { return dataAula; }
    public void setDataAula(LocalDateTime dataAula) { this.dataAula = dataAula; }

    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    @Override
    public String toString() {
        return "Aula{id=" + id + ", alunoId=" + alunoId + ", instrutorId=" + instrutorId +
               ", tipo='" + tipo + "', status='" + status + "'}";
    }
}