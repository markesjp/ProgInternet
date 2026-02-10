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

/**
 * DAO para tabela instrutores.
 * - CRUD completo
 * - Bloqueia DELETE se houver aulas vinculadas (boa prática para evitar erro de FK)
 */
public class InstrutorDao {

    private final ConnectionFactory factory;

    public InstrutorDao() {
        this.factory = new ConnectionFactory();
    }

    // =========================
    // Validações / Helpers
    // =========================

    /**
     * Verifica se já existe um instrutor com este CPF.
     */
    public boolean existeCpf(String cpf) {
        String sql = "SELECT 1 FROM instrutores WHERE cpf = ? LIMIT 1";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência de CPF: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se existe CPF em outro registro (para update).
     */
    public boolean existeCpfEmOutroId(String cpf, Integer idAtual) {
        String sql = "SELECT 1 FROM instrutores WHERE cpf = ? AND id <> ? LIMIT 1";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            stmt.setInt(2, idAtual);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF duplicado no update: " + e.getMessage(), e);
        }
    }

    // =========================
    // CREATE
    // =========================

    public void inserir(Instrutor instrutor) {
        String sql = "INSERT INTO instrutores (nome, cpf, telefone, especialidade, data_contratacao) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instrutor.getNome());
            stmt.setString(2, instrutor.getCpf());
            stmt.setString(3, instrutor.getTelefone());
            stmt.setString(4, instrutor.getEspecialidade());
            stmt.setDate(5, Date.valueOf(instrutor.getDataContratacao()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir instrutor: " + e.getMessage(), e);
        }
    }

    // =========================
    // READ
    // =========================

    public List<Instrutor> listar() {
        String sql = "SELECT * FROM instrutores ORDER BY nome ASC";
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

                if (rs.getDate("data_contratacao") != null) {
                    instrutor.setDataContratacao(rs.getDate("data_contratacao").toLocalDate());
                }

                instrutores.add(instrutor);
            }

            return instrutores;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar instrutores: " + e.getMessage(), e);
        }
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

                    if (rs.getDate("data_contratacao") != null) {
                        instrutor.setDataContratacao(rs.getDate("data_contratacao").toLocalDate());
                    }
                }
            }

            return instrutor;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar instrutor: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void alterar(Instrutor instrutor) {
        String sql = "UPDATE instrutores SET nome = ?, cpf = ?, telefone = ?, especialidade = ?, data_contratacao = ? " +
                     "WHERE id = ?";

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
            throw new RuntimeException("Erro ao alterar instrutor: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETE (com regra profissional)
    // =========================

    public void remover(Integer id) {
        // Bloqueia exclusão física quando houver aulas vinculadas (FK).
        String sqlCount = "SELECT COUNT(*) FROM aulas WHERE instrutor_id = ?";

        try (Connection conn = factory.getConnection();
             PreparedStatement cstmt = conn.prepareStatement(sqlCount)) {

            cstmt.setInt(1, id);

            int qtd;
            try (ResultSet rs = cstmt.executeQuery()) {
                rs.next();
                qtd = rs.getInt(1);
            }

            if (qtd > 0) {
                throw new RuntimeException(
                        "Não é possível remover instrutor: existe(m) " + qtd + " aula(s) vinculada(s). " +
                        "Realoque ou desmarque as aulas antes de remover."
                );
            }

            String sql = "DELETE FROM instrutores WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover instrutor: " + e.getMessage(), e);
        }
    }
}
