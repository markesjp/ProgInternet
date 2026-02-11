package autoescola.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Aula;
import autoescola.model.AulaDetalhada;

public class AulaDao {
    private final ConnectionFactory factory;

    public AulaDao() {
        this.factory = new ConnectionFactory();
    }

    /**
     * A tabela aulas possui CHECK: status IN ('MARCADA','DESMARCADA','CONCLUIDA')
     * Então qualquer valor fora disso precisa ser normalizado aqui.
     */
    private static String normalizarStatus(String s) {
        if (s == null) return "MARCADA";
        String up = s.trim().toUpperCase();
        if (up.isEmpty()) return "MARCADA";

        // ✅ compatibilidade com telas antigas:
        // AGENDADA -> MARCADA
        if ("AGENDADA".equals(up)) return "MARCADA";

        // variações/acentos
        if ("CONCLUÍDA".equals(up)) return "CONCLUIDA";
        if ("CONCLUIDA".equals(up)) return "CONCLUIDA";
        if ("MARCADA".equals(up)) return "MARCADA";
        if ("DESMARCADA".equals(up)) return "DESMARCADA";

        // fallback seguro
        return "MARCADA";
    }

    private Aula mapearAula(ResultSet rs) throws SQLException {
        Aula aula = new Aula();
        aula.setId(rs.getInt("id"));
        aula.setAlunoId(rs.getInt("aluno_id"));
        aula.setInstrutorId(rs.getInt("instrutor_id"));

        int veiculoId = rs.getInt("veiculo_id");
        if (rs.wasNull()) aula.setVeiculoId(null);
        else aula.setVeiculoId(veiculoId);

        aula.setDataAula(rs.getTimestamp("data_aula").toLocalDateTime());
        aula.setDuracaoMinutos(rs.getInt("duracao_minutos"));
        aula.setTipo(rs.getString("tipo"));
        aula.setStatus(rs.getString("status"));
        aula.setObservacoes(rs.getString("observacoes"));
        return aula;
    }

    private String buscarStatus(Connection conn, Integer aulaId) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement("SELECT status FROM aulas WHERE id=?")) {
            s.setInt(1, aulaId);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                return null;
            }
        }
    }

    private void inserirHistorico(Connection conn, Integer aulaId, String from, String to, String motivo, String actor)
            throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(
                "INSERT INTO aulas_historico(aula_id, from_status, to_status, motivo, actor) VALUES (?,?,?,?,?)")) {
            s.setInt(1, aulaId);
            s.setString(2, from);
            s.setString(3, to);
            s.setString(4, motivo);
            s.setString(5, (actor == null || actor.isBlank()) ? "system" : actor);
            s.executeUpdate();
        }
    }

    // ---------------------------
    // CREATE
    // ---------------------------
    public void inserir(Aula aula) {
        String sql = "INSERT INTO aulas (aluno_id, instrutor_id, veiculo_id, data_aula, duracao_minutos, tipo, status, observacoes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String status = normalizarStatus(aula.getStatus());

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = factory.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, aula.getAlunoId());
            stmt.setInt(2, aula.getInstrutorId());

            if (aula.getVeiculoId() != null) stmt.setInt(3, aula.getVeiculoId());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setTimestamp(4, Timestamp.valueOf(aula.getDataAula()));
            stmt.setInt(5, aula.getDuracaoMinutos());
            stmt.setString(6, aula.getTipo());
            stmt.setString(7, status); // ✅ sempre compatível com CHECK
            stmt.setString(8, aula.getObservacoes());

            stmt.executeUpdate();

            Integer aulaId = null;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) aulaId = keys.getInt(1);
            }

            if (aulaId != null) inserirHistorico(conn, aulaId, null, status, "Criação de aula", "user");

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Erro ao inserir aula: " + e.getMessage());
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    // ---------------------------
    // READ
    // ---------------------------
    public List<Aula> listar() {
        String sql = "SELECT * FROM aulas ORDER BY data_aula DESC";
        List<Aula> aulas = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) aulas.add(mapearAula(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas: " + e.getMessage());
        }

        return aulas;
    }

    public Aula buscarPorId(Integer id) {
        String sql = "SELECT * FROM aulas WHERE id = ?";
        Aula aula = null;

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) aula = mapearAula(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aula: " + e.getMessage());
        }

        return aula;
    }

    public List<AulaDetalhada> listarDetalhadas() {
        String sql = "SELECT a.id, a.aluno_id, al.nome AS aluno_nome, "
                + "a.instrutor_id, i.nome AS instrutor_nome, "
                + "a.veiculo_id, v.placa AS veiculo_placa, v.modelo AS veiculo_modelo, "
                + "a.data_aula, a.duracao_minutos, a.tipo, a.status, a.observacoes "
                + "FROM aulas a "
                + "INNER JOIN alunos al ON a.aluno_id = al.id "
                + "INNER JOIN instrutores i ON a.instrutor_id = i.id "
                + "LEFT JOIN veiculos v ON a.veiculo_id = v.id "
                + "ORDER BY a.data_aula DESC";

        List<AulaDetalhada> aulas = new ArrayList<>();

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

                d.setDataAula(rs.getTimestamp("data_aula").toLocalDateTime());
                d.setDuracaoMinutos(rs.getInt("duracao_minutos"));
                d.setTipo(rs.getString("tipo"));
                d.setStatus(rs.getString("status"));
                d.setObservacoes(rs.getString("observacoes"));

                aulas.add(d);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas detalhadas: " + e.getMessage());
        }

        return aulas;
    }

    // ✅ Usado no modal de desativação: buscar apenas MARCADAS (já compatível com CHECK)
    public List<AulaDetalhada> listarFuturasMarcadasDetalhadasPorAluno(Integer alunoId) {
        String sql = "SELECT a.id, a.aluno_id, al.nome AS aluno_nome, "
                + "a.instrutor_id, i.nome AS instrutor_nome, "
                + "a.veiculo_id, v.placa AS veiculo_placa, v.modelo AS veiculo_modelo, "
                + "a.data_aula, a.duracao_minutos, a.tipo, a.status, a.observacoes "
                + "FROM aulas a "
                + "INNER JOIN alunos al ON a.aluno_id = al.id "
                + "INNER JOIN instrutores i ON a.instrutor_id = i.id "
                + "LEFT JOIN veiculos v ON a.veiculo_id = v.id "
                + "WHERE a.aluno_id = ? "
                + "  AND a.data_aula > NOW() "
                + "  AND UPPER(a.status) = 'MARCADA' "
                + "ORDER BY a.data_aula ASC";

        List<AulaDetalhada> aulas = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alunoId);

            try (ResultSet rs = stmt.executeQuery()) {
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

                    d.setDataAula(rs.getTimestamp("data_aula").toLocalDateTime());
                    d.setDuracaoMinutos(rs.getInt("duracao_minutos"));
                    d.setTipo(rs.getString("tipo"));
                    d.setStatus(rs.getString("status"));
                    d.setObservacoes(rs.getString("observacoes"));

                    aulas.add(d);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas futuras do aluno: " + e.getMessage());
        }

        return aulas;
    }

    // ---------------------------
    // UPDATE
    // ---------------------------
    public void alterar(Aula aula) {
        String sql = "UPDATE aulas SET aluno_id=?, instrutor_id=?, veiculo_id=?, data_aula=?, duracao_minutos=?, tipo=?, status=?, observacoes=? WHERE id=?";

        String novoStatus = normalizarStatus(aula.getStatus());

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = factory.getConnection();
            conn.setAutoCommit(false);

            String statusAtual = buscarStatus(conn, aula.getId());
            if (statusAtual == null) throw new RuntimeException("Aula não encontrada.");

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, aula.getAlunoId());
            stmt.setInt(2, aula.getInstrutorId());

            if (aula.getVeiculoId() != null) stmt.setInt(3, aula.getVeiculoId());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setTimestamp(4, Timestamp.valueOf(aula.getDataAula()));
            stmt.setInt(5, aula.getDuracaoMinutos());
            stmt.setString(6, aula.getTipo());
            stmt.setString(7, novoStatus); // ✅ sempre compatível com CHECK
            stmt.setString(8, aula.getObservacoes());
            stmt.setInt(9, aula.getId());

            stmt.executeUpdate();

            if (!statusAtual.equalsIgnoreCase(novoStatus)) {
                inserirHistorico(conn, aula.getId(), statusAtual, novoStatus, "Alteração de aula", "user");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Erro ao alterar aula: " + e.getMessage());
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    // ---------------------------
    // DELETE (soft cancel -> DESMARCADA)
    // ---------------------------
    public void remover(Integer id) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = factory.getConnection();
            conn.setAutoCommit(false);

            String statusAtual = buscarStatus(conn, id);
            if (statusAtual == null) throw new RuntimeException("Aula não encontrada.");
            if ("CONCLUIDA".equalsIgnoreCase(statusAtual)) {
                throw new RuntimeException("Não é possível desmarcar uma aula concluída.");
            }

            stmt = conn.prepareStatement("UPDATE aulas SET status='DESMARCADA' WHERE id=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();

            inserirHistorico(conn, id, statusAtual, "DESMARCADA", "Desmarcação manual", "user");

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Erro ao desmarcar aula: " + e.getMessage());
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
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
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas por aluno: " + e.getMessage());
        }

        return aulas;
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
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aulas por instrutor: " + e.getMessage());
        }

        return aulas;
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private static void rollbackQuietly(Connection conn) {
        if (conn == null) return;
        try { conn.rollback(); } catch (Exception ignore) {}
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c == null) return;
        try { c.close(); } catch (Exception ignore) {}
    }
}
