package autoescola.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Aluno;

/**
 * DAO: alunos (MySQL)
 *
 * Melhorias:
 * - Status do aluno (ATIVO/INATIVO) com histórico (alunos_historico)
 * - Desativação profissional: ao desativar, desmarca aulas futuras MARCADAS (mantendo histórico)
 */
public class AlunoDao {
    private final ConnectionFactory factory;

    public AlunoDao() {
        this.factory = new ConnectionFactory();
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private Aluno mapearAluno(ResultSet rs) throws SQLException {
        Aluno a = new Aluno();
        a.setId(rs.getInt("id"));
        a.setNome(rs.getString("nome"));
        a.setCpf(rs.getString("cpf"));
        a.setTelefone(rs.getString("telefone"));
        a.setEmail(rs.getString("email"));

        Date dn = rs.getDate("data_nascimento");
        if (dn != null) a.setDataNascimento(dn.toLocalDate());

        a.setCategoriaDesejada(rs.getString("categoria_desejada"));

        Date dm = rs.getDate("data_matricula");
        if (dm != null) a.setDataMatricula(dm.toLocalDate());

        // status (ATIVO/INATIVO). Se não existir coluna (versão antiga), assume ATIVO.
        try {
            String status = rs.getString("status");
            a.setStatus((status == null || status.isBlank()) ? "ATIVO" : status);
        } catch (SQLException ignore) {
            a.setStatus("ATIVO");
        }

        return a;
    }

    private static String normStatus(String s) {
        if (s == null) return null;
        s = s.trim().toUpperCase();
        if (s.isEmpty()) return null;
        if (!"ATIVO".equals(s) && !"INATIVO".equals(s)) return null;
        return s;
    }

    // ---------------------------
    // Consultas simples
    // ---------------------------
    public boolean existeCpf(String cpf) {
        String sql = "SELECT 1 FROM alunos WHERE cpf = ? LIMIT 1";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF: " + e.getMessage(), e);
        }
    }

    public List<Aluno> listar() {
        String sql = "SELECT * FROM alunos ORDER BY id DESC";
        List<Aluno> alunos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) alunos.add(mapearAluno(rs));
            return alunos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos: " + e.getMessage(), e);
        }
    }

    public Aluno buscarPorId(Integer id) {
        String sql = "SELECT * FROM alunos WHERE id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearAluno(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno: " + e.getMessage(), e);
        }
    }

    public String buscarStatus(Integer alunoId) {
        String sql = "SELECT status FROM alunos WHERE id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String st = rs.getString(1);
                    return (st == null || st.isBlank()) ? "ATIVO" : st;
                }
                return null;
            }

        } catch (SQLException e) {
            // Se por acaso a coluna não existir na base antiga
            return "ATIVO";
        }
    }

    public Aluno buscarPorCpf(String cpfDigits, String statusFilter) {
        String st = normStatus(statusFilter);

        String sql = "SELECT * FROM alunos WHERE cpf = ?"
                + (st != null ? " AND status = ?" : "")
                + " LIMIT 1";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int i = 1;
            stmt.setString(i++, cpfDigits);
            if (st != null) stmt.setString(i++, st);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearAluno(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar por CPF: " + e.getMessage(), e);
        }
    }

    public List<Aluno> listarPorNome(String nome, String statusFilter) {
        String st = normStatus(statusFilter);

        String sql = "SELECT * FROM alunos WHERE nome LIKE ?"
                + (st != null ? " AND status = ?" : "")
                + " ORDER BY nome ASC";

        List<Aluno> alunos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int i = 1;
            stmt.setString(i++, "%" + nome + "%");
            if (st != null) stmt.setString(i++, st);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) alunos.add(mapearAluno(rs));
            }

            return alunos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar por nome: " + e.getMessage(), e);
        }
    }

    // ---------------------------
    // CRUD
    // ---------------------------
    public void inserir(Aluno aluno) {
        String sql = "INSERT INTO alunos (nome, cpf, telefone, email, data_nascimento, categoria_desejada, data_matricula, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String status = normStatus(aluno.getStatus());
        if (status == null) status = "ATIVO";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getTelefone());
            stmt.setString(4, aluno.getEmail());

            if (aluno.getDataNascimento() != null) stmt.setDate(5, Date.valueOf(aluno.getDataNascimento()));
            else stmt.setNull(5, Types.DATE);

            stmt.setString(6, aluno.getCategoriaDesejada());

            if (aluno.getDataMatricula() != null) stmt.setDate(7, Date.valueOf(aluno.getDataMatricula()));
            else stmt.setDate(7, Date.valueOf(java.time.LocalDate.now()));

            stmt.setString(8, status);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir aluno: " + e.getMessage(), e);
        }
    }

    public void alterar(Aluno aluno) {
        String sql = "UPDATE alunos SET nome=?, cpf=?, telefone=?, email=?, data_nascimento=?, categoria_desejada=? WHERE id=?";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getTelefone());
            stmt.setString(4, aluno.getEmail());

            if (aluno.getDataNascimento() != null) stmt.setDate(5, Date.valueOf(aluno.getDataNascimento()));
            else stmt.setNull(5, Types.DATE);

            stmt.setString(6, aluno.getCategoriaDesejada());
            stmt.setInt(7, aluno.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar aluno: " + e.getMessage(), e);
        }
    }

    /**
     * Remoção física é ruim para histórico e relacionamentos.
     * Mantemos o método por compatibilidade, mas ele DESATIVA.
     * @return quantidade de aulas futuras desmarcadas
     */
    public int remover(Integer id) {
        return desativarComDesmarcacaoFuturas(id, "Desativação via remover()", "system");
    }

    // ---------------------------
    // Regras profissionais (status + histórico)
    // ---------------------------

    public int contarAulasFuturasMarcadas(Integer alunoId) {
        // Seu padrão de status de aula é: MARCADA / DESMARCADA / CONCLUIDA
        // Mantive AGENDADA como tolerância caso você use em algum ponto.
        String sql = "SELECT COUNT(id) FROM aulas " +
                     "WHERE aluno_id = ? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar aulas futuras: " + e.getMessage(), e);
        }
    }

    /**
     * Desativa aluno e desmarca aulas futuras MARCADAS/AGENDADAS, registrando histórico.
     * @return quantidade de aulas desmarcadas
     */
    public int desativarComDesmarcacaoFuturas(Integer alunoId, String motivo, String actor) {
        String actorFinal = (actor == null || actor.isBlank()) ? "system" : actor;
        String motivoFinal = (motivo == null || motivo.isBlank()) ? null : motivo.trim();
        int desmarcadas = 0;

        Connection conn = null;
        try {
            conn = factory.getConnection();
            conn.setAutoCommit(false);

            // 1) status atual
            String stAtual = "ATIVO";
            try (PreparedStatement s0 = conn.prepareStatement("SELECT status FROM alunos WHERE id = ?")) {
                s0.setInt(1, alunoId);
                try (ResultSet rs = s0.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Aluno não encontrado.");
                    stAtual = rs.getString(1);
                    if (stAtual == null || stAtual.isBlank()) stAtual = "ATIVO";
                }
            } catch (SQLException e) {
                stAtual = "ATIVO";
            }

            if ("INATIVO".equalsIgnoreCase(stAtual)) {
                conn.rollback();
                return 0;
            }

            // 2) atualiza aluno
            try (PreparedStatement s1 = conn.prepareStatement("UPDATE alunos SET status='INATIVO' WHERE id=?")) {
                s1.setInt(1, alunoId);
                s1.executeUpdate();
            }

            // 3) histórico aluno (se tabela existir)
            try (PreparedStatement s2 = conn.prepareStatement(
                    "INSERT INTO alunos_historico(aluno_id, from_status, to_status, motivo, actor) VALUES (?,?,?,?,?)")) {
                s2.setInt(1, alunoId);
                s2.setString(2, stAtual);
                s2.setString(3, "INATIVO");
                s2.setString(4, motivoFinal);
                s2.setString(5, actorFinal);
                s2.executeUpdate();
            } catch (SQLException ignore) {}

            // 4) histórico das aulas futuras marcadas -> desmarcadas (se tabela existir)
            String insertHistAulas =
                "INSERT INTO aulas_historico(aula_id, from_status, to_status, motivo, actor) " +
                "SELECT id, status, 'DESMARCADA', ?, ? FROM aulas " +
                "WHERE aluno_id = ? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')";
            try (PreparedStatement s3 = conn.prepareStatement(insertHistAulas)) {
                s3.setString(1, motivoFinal != null ? motivoFinal : "Aluno desativado (desmarcação automática)");
                s3.setString(2, actorFinal);
                s3.setInt(3, alunoId);
                s3.executeUpdate();
            } catch (SQLException ignore) {}

            // 5) update aulas futuras
            try (PreparedStatement s4 = conn.prepareStatement(
                "UPDATE aulas SET status='DESMARCADA' WHERE aluno_id=? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')")) {
                s4.setInt(1, alunoId);
                desmarcadas = s4.executeUpdate();
            }

            conn.commit();
            return desmarcadas;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Erro ao desativar aluno: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public void ativar(Integer alunoId, String motivo, String actor) {
        String actorFinal = (actor == null || actor.isBlank()) ? "system" : actor;
        String motivoFinal = (motivo == null || motivo.isBlank()) ? null : motivo.trim();

        Connection conn = null;
        try {
            conn = factory.getConnection();
            conn.setAutoCommit(false);

            String stAtual = "INATIVO";
            try (PreparedStatement s0 = conn.prepareStatement("SELECT status FROM alunos WHERE id = ?")) {
                s0.setInt(1, alunoId);
                try (ResultSet rs = s0.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Aluno não encontrado.");
                    stAtual = rs.getString(1);
                    if (stAtual == null || stAtual.isBlank()) stAtual = "ATIVO";
                }
            } catch (SQLException e) {
                stAtual = "ATIVO";
            }

            try (PreparedStatement s1 = conn.prepareStatement("UPDATE alunos SET status='ATIVO' WHERE id=?")) {
                s1.setInt(1, alunoId);
                s1.executeUpdate();
            }

            try (PreparedStatement s2 = conn.prepareStatement(
                "INSERT INTO alunos_historico(aluno_id, from_status, to_status, motivo, actor) VALUES (?,?,?,?,?)")) {
                s2.setInt(1, alunoId);
                s2.setString(2, stAtual);
                s2.setString(3, "ATIVO");
                s2.setString(4, motivoFinal);
                s2.setString(5, actorFinal);
                s2.executeUpdate();
            } catch (SQLException ignore) {}

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Erro ao ativar aluno: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
