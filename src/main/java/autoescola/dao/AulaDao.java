package autoescola.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Aula;
import autoescola.model.AulaDetalhada;

/**
 * DAO de Aulas (PostgreSQL).
 *
 * Regras profissionais aplicadas:
 * - "Remover" não apaga: muda status para DESMARCADA e registra histórico.
 * - Não desmarca CONCLUIDA.
 * - Aulas teóricas podem ter veiculo_id NULL.
 * - Histórico em aulas_historico.
 */
public class AulaDao {

    private final ConnectionFactory factory;

    public AulaDao() {
        this.factory = new ConnectionFactory();
    }

    // =========================
    // Helpers
    // =========================

    private Aula mapearAula(ResultSet rs) throws SQLException {
        Aula aula = new Aula();
        aula.setId(rs.getInt("id"));
        aula.setAlunoId(rs.getInt("aluno_id"));
        aula.setInstrutorId(rs.getInt("instrutor_id"));

        int veiculoId = rs.getInt("veiculo_id");
        if (rs.wasNull()) aula.setVeiculoId(null);
        else aula.setVeiculoId(veiculoId);

        if (rs.getTimestamp("data_aula") != null) {
            aula.setDataAula(rs.getTimestamp("data_aula").toLocalDateTime());
        }

        aula.setDuracaoMinutos(rs.getInt("duracao_minutos"));
        aula.setTipo(rs.getString("tipo"));
        aula.setStatus(rs.getString("status"));
        aula.setObservacoes(rs.getString("observacoes"));
        return aula;
    }

    /**
     * Normaliza status para o padrão do banco (PostgreSQL) e do projeto:
     * MARCADA / DESMARCADA / CONCLUIDA.
     */
    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) return "MARCADA";
        String s = status.trim().toUpperCase();

        // tolerância a variações
        if (s.equals("CONCLUÍDA")) s = "CONCLUIDA";
        if (s.equals("CANCELADA") || s.equals("CANCELADO")) s = "DESMARCADA";
        if (s.equals("AGENDADA")) s = "MARCADA";

        // garante apenas valores válidos
        if (!s.equals("MARCADA") && !s.equals("DESMARCADA") && !s.equals("CONCLUIDA")) {
            return "MARCADA";
        }
        return s;
    }

    private String buscarStatus(Connection conn, Integer aulaId) throws SQLException {
        String sql = "SELECT status FROM aulas WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, aulaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return null;
    }

    private void inserirHistorico(Connection conn,
                                  Integer aulaId,
                                  String fromStatus,
                                  String toStatus,
                                  String motivo,
                                  String actor) throws SQLException {

        if (aulaId == null) return;

        String sql = "INSERT INTO aulas_historico (aula_id, changed_at, from_status, to_status, motivo, actor) " +
                     "VALUES (?, NOW(), ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, aulaId);

            if (fromStatus == null || fromStatus.isBlank()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, normalizarStatus(fromStatus));

            ps.setString(3, normalizarStatus(toStatus));
            ps.setString(4, motivo);
            ps.setString(5, actor);

            ps.executeUpdate();
        }
    }

    // =========================
    // CREATE
    // =========================

    public void inserir(Aula aula) {
        String sql = "INSERT INTO aulas (aluno_id, instrutor_id, veiculo_id, data_aula, duracao_minutos, tipo, status, observacoes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String status = normalizarStatus(aula.getStatus());
        String tipo = (aula.getTipo() == null ? "PRATICA" : aula.getTipo().trim().toUpperCase());

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, aula.getAlunoId());
            stmt.setInt(2, aula.getInstrutorId());

            if (aula.getVeiculoId() != null) stmt.setInt(3, aula.getVeiculoId());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setTimestamp(4, Timestamp.valueOf(aula.getDataAula()));
            stmt.setInt(5, aula.getDuracaoMinutos());
            stmt.setString(6, tipo);
            stmt.setString(7, status);
            stmt.setString(8, aula.getObservacoes());

            stmt.executeUpdate();

            Integer aulaId = null;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) aulaId = keys.getInt(1);
            }

            inserirHistorico(conn, aulaId, null, status, "Criação de aula", "system");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir aula: " + e.getMessage(), e);
        }
    }

    // =========================
    // READ
    // =========================

    public List<Aula> listar() {
        String sql = "SELECT * FROM aulas ORDER BY data_aula DESC";
        List<Aula> aulas = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                aulas.add(mapearAula(rs));
            }
            return aulas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas: " + e.getMessage(), e);
        }
    }

    /**
     * Lista detalhada (para tela): JOIN com nomes do aluno/instrutor e dados do veículo.
     */
    public List<AulaDetalhada> listarDetalhadas() {
        String sql =
            "SELECT a.id, a.aluno_id, al.nome AS aluno_nome, " +
            "       a.instrutor_id, i.nome AS instrutor_nome, " +
            "       a.veiculo_id, v.placa AS veiculo_placa, v.modelo AS veiculo_modelo, " +
            "       a.data_aula, a.duracao_minutos, a.tipo, a.status, a.observacoes " +
            "FROM aulas a " +
            "INNER JOIN alunos al ON a.aluno_id = al.id " +
            "INNER JOIN instrutores i ON a.instrutor_id = i.id " +
            "LEFT JOIN veiculos v ON a.veiculo_id = v.id " +
            "ORDER BY a.data_aula DESC";

        List<AulaDetalhada> out = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AulaDetalhada d = new AulaDetalhada();
                d.setId(rs.getInt("id"));

                d.setAlunoId(rs.getInt("aluno_id"));
                d.setAlunoNome(rs.getString("aluno_nome"));

                d.setInstrutorId(rs.getInt("instrutor_id"));
                d.setInstrutorNome(rs.getString("instrutor_nome"));

                int veiculoId = rs.getInt("veiculo_id");
                if (rs.wasNull()) d.setVeiculoId(null);
                else d.setVeiculoId(veiculoId);

                d.setVeiculoPlaca(rs.getString("veiculo_placa"));
                d.setVeiculoModelo(rs.getString("veiculo_modelo"));

                if (rs.getTimestamp("data_aula") != null) {
                    d.setDataAula(rs.getTimestamp("data_aula").toLocalDateTime());
                }

                d.setDuracaoMinutos(rs.getInt("duracao_minutos"));
                d.setTipo(rs.getString("tipo"));
                d.setStatus(rs.getString("status"));
                d.setObservacoes(rs.getString("observacoes"));

                out.add(d);
            }

            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas detalhadas: " + e.getMessage(), e);
        }
    }

    public Aula buscarPorId(Integer id) {
        String sql = "SELECT * FROM aulas WHERE id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearAula(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aula: " + e.getMessage(), e);
        }
    }

    public List<Aula> listarPorAluno(Integer alunoId) {
        String sql = "SELECT * FROM aulas WHERE aluno_id = ? ORDER BY data_aula DESC";
        List<Aula> aulas = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) aulas.add(mapearAula(rs));
            }
            return aulas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas por aluno: " + e.getMessage(), e);
        }
    }

    public List<Aula> listarPorInstrutor(Integer instrutorId) {
        String sql = "SELECT * FROM aulas WHERE instrutor_id = ? ORDER BY data_aula DESC";
        List<Aula> aulas = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instrutorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) aulas.add(mapearAula(rs));
            }
            return aulas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas por instrutor: " + e.getMessage(), e);
        }
    }

    /**
     * Apenas para debug/log (como no seu método antigo).
     */
    public List<String> listarAulasCompletas() {
        String sql =
            "SELECT a.id, al.nome AS aluno_nome, i.nome AS instrutor_nome, " +
            "       v.placa, v.modelo, a.data_aula, a.tipo, a.status " +
            "FROM aulas a " +
            "INNER JOIN alunos al ON a.aluno_id = al.id " +
            "INNER JOIN instrutores i ON a.instrutor_id = i.id " +
            "LEFT JOIN veiculos v ON a.veiculo_id = v.id " +
            "ORDER BY a.data_aula DESC";

        List<String> out = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String veiculoInfo = (rs.getString("modelo") != null)
                        ? String.format("%s (%s)", rs.getString("modelo"), rs.getString("placa"))
                        : "N/A (Teórica)";

                String detalhes = String.format(
                        "Aula #%d | Aluno: %s | Instrutor: %s | Veículo: %s | Data: %s | Tipo: %s | Status: %s",
                        rs.getInt("id"),
                        rs.getString("aluno_nome"),
                        rs.getString("instrutor_nome"),
                        veiculoInfo,
                        rs.getTimestamp("data_aula"),
                        rs.getString("tipo"),
                        rs.getString("status")
                );
                out.add(detalhes);
            }

            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas completas: " + e.getMessage(), e);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void alterar(Aula aula) {
        String sql =
            "UPDATE aulas SET aluno_id=?, instrutor_id=?, veiculo_id=?, data_aula=?, duracao_minutos=?, tipo=?, status=?, observacoes=? " +
            "WHERE id=?";

        String novoStatus = normalizarStatus(aula.getStatus());
        String tipo = (aula.getTipo() == null ? "PRATICA" : aula.getTipo().trim().toUpperCase());

        try (Connection conn = factory.getConnection()) {
            conn.setAutoCommit(false);

            String statusAtual = buscarStatus(conn, aula.getId());
            if (statusAtual == null) {
                conn.rollback();
                throw new RuntimeException("Aula não encontrada.");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, aula.getAlunoId());
                stmt.setInt(2, aula.getInstrutorId());

                if (aula.getVeiculoId() != null) stmt.setInt(3, aula.getVeiculoId());
                else stmt.setNull(3, Types.INTEGER);

                stmt.setTimestamp(4, Timestamp.valueOf(aula.getDataAula()));
                stmt.setInt(5, aula.getDuracaoMinutos());
                stmt.setString(6, tipo);
                stmt.setString(7, novoStatus);
                stmt.setString(8, aula.getObservacoes());
                stmt.setInt(9, aula.getId());

                stmt.executeUpdate();
            }

            if (!statusAtual.equalsIgnoreCase(novoStatus)) {
                inserirHistorico(conn, aula.getId(), statusAtual, novoStatus, "Alteração de aula", "system");
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar aula: " + e.getMessage(), e);
        }
    }

    // =========================
    // DELETE (soft)
    // =========================

    /**
     * "Remover" = DESMARCAR (soft delete).
     * Não altera CONCLUIDA.
     */
    public void remover(Integer id) {
        String sql = "UPDATE aulas SET status='DESMARCADA' WHERE id = ?";

        try (Connection conn = factory.getConnection()) {
            conn.setAutoCommit(false);

            String statusAtual = buscarStatus(conn, id);
            if (statusAtual == null) {
                conn.rollback();
                throw new RuntimeException("Aula não encontrada.");
            }
            if ("CONCLUIDA".equalsIgnoreCase(statusAtual) || "CONCLUÍDA".equalsIgnoreCase(statusAtual)) {
                conn.rollback();
                throw new RuntimeException("Não é possível desmarcar uma aula concluída.");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            inserirHistorico(conn, id, statusAtual, "DESMARCADA", "Desmarcação manual", "system");
            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desmarcar aula: " + e.getMessage(), e);
        }
    }
}
