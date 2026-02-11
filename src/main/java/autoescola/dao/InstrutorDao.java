package autoescola.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Instrutor;

public class InstrutorDao {
    private final ConnectionFactory factory;

    public InstrutorDao() {
        this.factory = new ConnectionFactory();
    }

    public boolean existeCpf(String cpf) {
        String sql = "SELECT COUNT(id) FROM instrutores WHERE cpf = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência de CPF: " + e.getMessage());
        }
        return false;
    }

    public void inserir(Instrutor instrutor) {
        String sql = "INSERT INTO instrutores (nome, cpf, telefone, especialidade, data_contratacao) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instrutor.getNome());
            stmt.setString(2, instrutor.getCpf());
            stmt.setString(3, instrutor.getTelefone());
            stmt.setString(4, instrutor.getEspecialidade());
            stmt.setDate(5, Date.valueOf(instrutor.getDataContratacao()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir instrutor: " + e.getMessage());
        }
    }

    public List<Instrutor> listar() {
        String sql = "SELECT * FROM instrutores ORDER BY id DESC";
        List<Instrutor> instrutores = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Instrutor instrutor = new Instrutor();
                instrutor.setId(rs.getInt("id"));
                instrutor.setNome(rs.getString("nome"));
                instrutor.setCpf(rs.getString("cpf"));
                instrutor.setTelefone(rs.getString("telefone"));
                instrutor.setEspecialidade(rs.getString("especialidade"));
                instrutor.setDataContratacao(rs.getDate("data_contratacao").toLocalDate());
                instrutores.add(instrutor);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar instrutores: " + e.getMessage());
        }

        return instrutores;
    }

    public Instrutor buscarPorId(Integer id) {
        String sql = "SELECT * FROM instrutores WHERE id = ?";
        Instrutor instrutor = null;

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    instrutor = new Instrutor();
                    instrutor.setId(rs.getInt("id"));
                    instrutor.setNome(rs.getString("nome"));
                    instrutor.setCpf(rs.getString("cpf"));
                    instrutor.setTelefone(rs.getString("telefone"));
                    instrutor.setEspecialidade(rs.getString("especialidade"));
                    instrutor.setDataContratacao(rs.getDate("data_contratacao").toLocalDate());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar instrutor: " + e.getMessage());
        }

        return instrutor;
    }

    public void alterar(Instrutor instrutor) {
        String sql = "UPDATE instrutores SET nome = ?, cpf = ?, telefone = ?, especialidade = ?, data_contratacao = ? WHERE id = ?";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instrutor.getNome());
            stmt.setString(2, instrutor.getCpf());
            stmt.setString(3, instrutor.getTelefone());
            stmt.setString(4, instrutor.getEspecialidade());
            stmt.setDate(5, Date.valueOf(instrutor.getDataContratacao()));
            stmt.setInt(6, instrutor.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar instrutor: " + e.getMessage());
        }
    }

    public void remover(Integer id) {
        String sqlCount = "SELECT COUNT(id) FROM aulas WHERE instrutor_id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement cstmt = conn.prepareStatement(sqlCount)) {

            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
                rs.next();
                int qtd = rs.getInt(1);
                if (qtd > 0) {
                    throw new RuntimeException("Não é possível remover instrutor: existe(m) " + qtd
                            + " aula(s) vinculada(s). Realoque/desmarque as aulas antes de remover.");
                }
            }

            String sql = "DELETE FROM instrutores WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover instrutor: " + e.getMessage());
        }
    }
}
