package autoescola.model;

import java.time.LocalDate;

// Essa classe é o "molde" para um Instrutor.
// Ela guarda todas as informações que um instrutor tem no sistema.
public class Instrutor {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private String especialidade; // Ex: Categoria "A", "B", "D"
    private LocalDate dataContratacao;

    // Construtor vazio, útil para criar um objeto e preencher os dados depois.
    public Instrutor() {}

    // Construtor que já recebe os dados para criar um instrutor completo.
    public Instrutor(String nome, String cpf, String telefone,
                     String especialidade, LocalDate dataContratacao) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.especialidade = especialidade;
        this.dataContratacao = dataContratacao;
    }

    // Getters e Setters: métodos para pegar (get) e definir (set) os valores dos atributos.
    // Eles ajudam a proteger os dados da classe.
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public LocalDate getDataContratacao() { return dataContratacao; }
    public void setDataContratacao(LocalDate dataContratacao) { this.dataContratacao = dataContratacao; }

    // Um jeito legal de exibir as informações do instrutor quando a gente der um `System.out.println()`.
    @Override
    public String toString() {
        return "Instrutor{id=" + id + ", nome='" + nome + "', cpf='" + cpf +
               "', especialidade='" + especialidade + "'}";
    }
}