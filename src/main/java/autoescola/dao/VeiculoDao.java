package autoescola.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Veiculo;

public class VeiculoDao {
    private final ConnectionFactory factory;

    public VeiculoDao() {
        this.factory = new ConnectionFactory();
    }

    public boolean existePlaca(String placa) {
        String sql = "SELECT COUNT(id) FROM veiculos WHERE placa = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência da placa: " + e.getMessage());
        }
        return false;
    }

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

    public void inserir(Veiculo veiculo) {
        String sql = "INSERT INTO veiculos (placa, modelo, marca, ano, categoria, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getModelo());
            stmt.setString(3, veiculo.getMarca());
            stmt.setInt(4, veiculo.getAno());
            stmt.setString(5, veiculo.getCategoria());
            stmt.setString(6, veiculo.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir veículo: " + e.getMessage());
        }
    }

    public List<Veiculo> listar() {
        String sql = "SELECT * FROM veiculos ORDER BY id DESC";
        List<Veiculo> veiculos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) veiculos.add(mapearVeiculo(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos: " + e.getMessage());
        }

        return veiculos;
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
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar veículo: " + e.getMessage());
        }

        return veiculo;
    }

    public void alterar(Veiculo veiculo) {
        String sql = "UPDATE veiculos SET placa = ?, modelo = ?, marca = ?, ano = ?, categoria = ?, status = ? WHERE id = ?";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getModelo());
            stmt.setString(3, veiculo.getMarca());
            stmt.setInt(4, veiculo.getAno());
            stmt.setString(5, veiculo.getCategoria());
            stmt.setString(6, veiculo.getStatus());
            stmt.setInt(7, veiculo.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar veículo: " + e.getMessage());
        }
    }

    public void remover(Integer id) {
        String sqlCount = "SELECT COUNT(id) FROM aulas WHERE veiculo_id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement cstmt = conn.prepareStatement(sqlCount)) {

            cstmt.setInt(1, id);
            try (ResultSet rs = cstmt.executeQuery()) {
                rs.next();
                int qtd = rs.getInt(1);
                if (qtd > 0) {
                    throw new RuntimeException("Não é possível remover veículo: existe(m) " + qtd
                            + " aula(s) vinculada(s). Realoque/desmarque as aulas antes de remover.");
                }
            }

            String sql = "DELETE FROM veiculos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover veículo: " + e.getMessage());
        }
    }

    public List<Veiculo> listarDisponiveis(String categoria) {
        String sql = "SELECT * FROM veiculos WHERE status = 'DISPONIVEL' AND categoria = ?";
        List<Veiculo> veiculos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) veiculos.add(mapearVeiculo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos disponíveis: " + e.getMessage());
        }

        return veiculos;
    }
}
