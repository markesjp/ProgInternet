package autoescola.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Veiculo;

/**
 * DAO para tabela veiculos.
 * - CRUD completo
 * - Bloqueia DELETE se houver aulas vinculadas (boa prática para evitar erro de FK)
 */
public class VeiculoDao {

    private final ConnectionFactory factory;

    public VeiculoDao() {
        this.factory = new ConnectionFactory();
    }

    // =========================
    // Helpers
    // =========================

    private Veiculo mapearVeiculo(ResultSet rs) throws SQLException {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(rs.getInt("id"));
        veiculo.setPlaca(rs.getString("placa"));
        veiculo.setModelo(rs.getString("modelo"));
        veiculo.setMarca(rs.getString("marca"));
        veiculo.setAno(rs.getInt("ano"));
        veiculo.setCategoria(rs.getString("categoria"));
        veiculo.setStatus(rs.getString("status"));
        return veiculo;
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) return "DISPONIVEL";
        String s = status.trim().toUpperCase();

        if (!s.equals("DISPONIVEL") && !s.equals("MANUTENCAO") && !s.equals("INDISPONIVEL")) {
            return "DISPONIVEL";
        }
        return s;
    }

    // =========================
    // Validações
    // =========================

    /**
     * Verifica se já existe um veículo com esta placa.
     */
    public boolean existePlaca(String placa) {
        String sql = "SELECT 1 FROM veiculos WHERE placa = ? LIMIT 1";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placa);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência da placa: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica placa em outro registro (para update).
     */
    public boolean existePlacaEmOutroId(String placa, Integer idAtual) {
        String sql = "SELECT 1 FROM veiculos WHERE placa = ? AND id <> ? LIMIT 1";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placa);
            stmt.setInt(2, idAtual);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar placa duplicada no update: " + e.getMessage(), e);
        }
    }

    // =========================
    // CREATE
    // =========================

    public void inserir(Veiculo veiculo) {
        String sql = "INSERT INTO veiculos (placa, modelo, marca, ano, categoria, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getModelo());
            stmt.setString(3, veiculo.getMarca());
            stmt.setInt(4, veiculo.getAno());
            stmt.setString(5, veiculo.getCategoria());
            stmt.setString(6, normalizarStatus(veiculo.getStatus()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir veículo: " + e.getMessage(), e);
        }
    }

    // =========================
    // READ
    // =========================

    public List<Veiculo> listar() {
        String sql = "SELECT * FROM veiculos ORDER BY status ASC, marca ASC, modelo ASC";
        List<Veiculo> veiculos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                veiculos.add(mapearVeiculo(rs));
            }
            return veiculos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos: " + e.getMessage(), e);
        }
    }

    public Veiculo buscarPorId(Integer id) {
        String sql = "SELECT * FROM veiculos WHERE id = ?";
        Veiculo veiculo = null;

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) veiculo = mapearVeiculo(rs);
            }

            return veiculo;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar veículo: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void alterar(Veiculo veiculo) {
        String sql = "UPDATE veiculos SET placa = ?, modelo = ?, marca = ?, ano = ?, categoria = ?, status = ? WHERE id = ?";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getModelo());
            stmt.setString(3, veiculo.getMarca());
            stmt.setInt(4, veiculo.getAno());
            stmt.setString(5, veiculo.getCategoria());
            stmt.setString(6, normalizarStatus(veiculo.getStatus()));
            stmt.setInt(7, veiculo.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar veículo: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETE (com regra profissional)
    // =========================

    public void remover(Integer id) {
        // Bloqueia exclusão física quando houver aulas vinculadas (FK).
        String sqlCount = "SELECT COUNT(*) FROM aulas WHERE veiculo_id = ?";

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
                        "Não é possível remover veículo: existe(m) " + qtd + " aula(s) vinculada(s). " +
                        "Realoque ou desmarque as aulas antes de remover."
                );
            }

            String sql = "DELETE FROM veiculos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover veículo: " + e.getMessage(), e);
        }
    }

    // =========================
    // Extras
    // =========================

    /**
     * Lista veículos disponíveis de uma categoria.
     */
    public List<Veiculo> listarDisponiveis(String categoria) {
        String sql = "SELECT * FROM veiculos WHERE status = 'DISPONIVEL' AND categoria = ? ORDER BY marca, modelo";
        List<Veiculo> veiculos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) veiculos.add(mapearVeiculo(rs));
            }

            return veiculos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos disponíveis: " + e.getMessage(), e);
        }
    }
}
