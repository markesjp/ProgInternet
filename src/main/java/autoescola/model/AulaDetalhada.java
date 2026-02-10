package autoescola.model;

import java.time.LocalDateTime;

/**
 * DTO (objeto de transporte) para exibir a aula com informações mais "humanas"
 * no frontend (nome do aluno, nome do instrutor e dados do veículo).
 */
public class AulaDetalhada {

    private Integer id;

    private Integer alunoId;
    private String alunoNome;

    private Integer instrutorId;
    private String instrutorNome;

    private Integer veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;

    private LocalDateTime dataAula;
    private Integer duracaoMinutos;
    private String tipo;
    private String status;
    private String observacoes;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public Integer getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Integer instrutorId) { this.instrutorId = instrutorId; }

    public String getInstrutorNome() { return instrutorNome; }
    public void setInstrutorNome(String instrutorNome) { this.instrutorNome = instrutorNome; }

    public Integer getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Integer veiculoId) { this.veiculoId = veiculoId; }

    public String getVeiculoPlaca() { return veiculoPlaca; }
    public void setVeiculoPlaca(String veiculoPlaca) { this.veiculoPlaca = veiculoPlaca; }

    public String getVeiculoModelo() { return veiculoModelo; }
    public void setVeiculoModelo(String veiculoModelo) { this.veiculoModelo = veiculoModelo; }

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
}
