package autoescola.model;

// Essa classe representa um Veículo da autoescola (carro, moto, etc.).
public class Veiculo {
    private Integer id;
    private String placa;
    private String modelo;
    private String marca;
    private Integer ano;
    private String categoria; // "A", "B", etc.
    private String status; // "DISPONIVEL", "MANUTENCAO", etc.

    // Construtor vazio.
    public Veiculo() {}

    // Construtor para criar um veículo já com seus dados.
    public Veiculo(String placa, String modelo, String marca,
                   Integer ano, String categoria, String status) {
        this.placa = placa;
        this.modelo = modelo;
        this.marca = marca;
        this.ano = ano;
        this.categoria = categoria;
        this.status = status;
    }

    // Getters e Setters para os atributos do veículo.
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Veiculo{id=" + id + ", placa='" + placa + "', modelo='" + modelo +
               "', marca='" + marca + "', categoria='" + categoria + "'}";
    }
}