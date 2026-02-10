package autoescola.model;

import java.time.LocalDate;

// Essa classe é o "molde" para um Aluno.
// Ela guarda todos os dados importantes de um aluno.
public class Aluno {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private LocalDate dataNascimento;
    private String categoriaDesejada; // Ex: "A", "B", "AB"
    private String status; // ATIVO | INATIVO
    private LocalDate dataMatricula;

    // Construtor vazio.
    public Aluno() {}

    // Construtor para criar um aluno já com todos os seus dados.
    public Aluno(String nome, String cpf, String telefone, String email,
                 LocalDate dataNascimento, String categoriaDesejada, LocalDate dataMatricula) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.categoriaDesejada = categoriaDesejada;
        this.dataMatricula = dataMatricula;
    }

    // Getters e Setters para acessar e modificar os atributos de forma controlada.
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getCategoriaDesejada() { return categoriaDesejada; }
    public void setCategoriaDesejada(String categoriaDesejada) { this.categoriaDesejada = categoriaDesejada; }

    public LocalDate getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDate dataMatricula) { this.dataMatricula = dataMatricula; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Método para facilitar a visualização dos dados do aluno.
    @Override
    public String toString() {
        return "Aluno{id=" + id + ", nome='" + nome + "', cpf='" + cpf +
               "', categoria='" + categoriaDesejada + "'}";
    }
}