package autoescola.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoescola.factory.ConnectionFactory;
import autoescola.model.Aluno;

public class AlunoDao {
    private final ConnectionFactory factory;

    public AlunoDao() {
        this.factory = new ConnectionFactory();
    }

    private Aluno map(ResultSet rs) throws SQLException {
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

        String status = null;
        try { status = rs.getString("status"); } catch (SQLException ignore) {}
        a.setStatus((status == null || status.isBlank()) ? "ATIVO" : status.toUpperCase());

        return a;
    }

    public boolean existeCpf(String cpf) {
        String sql = "SELECT COUNT(id) FROM alunos WHERE cpf = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF: " + e.getMessage());
        }
    }

    public List<Aluno> listar() {
        String sql = "SELECT * FROM alunos ORDER BY id DESC";
        List<Aluno> alunos = new ArrayList<>();
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) alunos.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos: " + e.getMessage());
        }
        return alunos;
    }

    public Aluno buscarPorId(Integer id) {
        String sql = "SELECT * FROM alunos WHERE id = ?";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por ID: " + e.getMessage());
        }
    }

    // =========================================================
    // Métodos usados pelo BuscaAlunoServlet
    // =========================================================
    public Aluno buscarPorCpf(String cpfDigits, String statusUpperOrEmpty) {
        String st = (statusUpperOrEmpty == null) ? "" : statusUpperOrEmpty.trim().toUpperCase();
        boolean filtraStatus = !st.isEmpty();

        String sql = "SELECT * FROM alunos WHERE cpf = ?"
                   + (filtraStatus ? " AND UPPER(status) = ?" : "");

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpfDigits);
            if (filtraStatus) stmt.setString(2, st);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar por CPF: " + e.getMessage());
        }
    }

    public List<Aluno> listarPorNome(String nome, String statusUpperOrEmpty) {
        String st = (statusUpperOrEmpty == null) ? "" : statusUpperOrEmpty.trim().toUpperCase();
        boolean filtraStatus = !st.isEmpty();

        String sql = "SELECT * FROM alunos WHERE LOWER(nome) LIKE ?"
                   + (filtraStatus ? " AND UPPER(status) = ?" : "")
                   + " ORDER BY nome ASC";

        List<Aluno> alunos = new ArrayList<>();

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + (nome == null ? "" : nome.trim().toLowerCase()) + "%");
            if (filtraStatus) stmt.setString(2, st);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) alunos.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar por nome: " + e.getMessage());
        }

        return alunos;
    }

    // =========================================================
    // Status + Desmarcação de aulas futuras
    // =========================================================
    public int contarAulasFuturasMarcadas(Integer alunoId) {
        String sql = "SELECT COUNT(id) FROM aulas "
                   + "WHERE aluno_id = ? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')";
        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar aulas futuras: " + e.getMessage());
        }
    }

    public int desativarComDesmarcacaoFuturas(Integer alunoId, String motivo, String actor) {
        String actorFinal = (actor == null || actor.isBlank()) ? "system" : actor;
        String motivoFinal = (motivo == null || motivo.isBlank()) ? null : motivo.trim();

        int desmarcadas = 0;

        try (Connection conn = factory.getConnection()) {
            conn.setAutoCommit(false);

            String statusAtual = "ATIVO";
            try (PreparedStatement s0 = conn.prepareStatement("SELECT status FROM alunos WHERE id = ?")) {
                s0.setInt(1, alunoId);
                try (ResultSet rs = s0.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Aluno não encontrado.");
                    statusAtual = rs.getString(1);
                    if (statusAtual == null || statusAtual.isBlank()) statusAtual = "ATIVO";
                }
            }

            if ("INATIVO".equalsIgnoreCase(statusAtual)) {
                conn.rollback();
                return 0;
            }

            try (PreparedStatement s1 = conn.prepareStatement("UPDATE alunos SET status='INATIVO' WHERE id=?")) {
                s1.setInt(1, alunoId);
                s1.executeUpdate();
            }

            // histórico do aluno (se existir)
            try (PreparedStatement s2 = conn.prepareStatement(
                    "INSERT INTO alunos_historico(aluno_id, from_status, to_status, motivo, actor) VALUES (?,?,?,?,?)")) {
                s2.setInt(1, alunoId);
                s2.setString(2, statusAtual);
                s2.setString(3, "INATIVO");
                s2.setString(4, motivoFinal);
                s2.setString(5, actorFinal);
                s2.executeUpdate();
            } catch (SQLException ignore) {}

            // histórico das aulas (se existir)
            try (PreparedStatement s3 = conn.prepareStatement(
                "INSERT INTO aulas_historico(aula_id, from_status, to_status, motivo, actor) " +
                "SELECT id, status, 'DESMARCADA', ?, ? FROM aulas " +
                "WHERE aluno_id = ? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')")) {
                s3.setString(1, motivoFinal != null ? motivoFinal : "Aluno desativado (desmarcação automática)");
                s3.setString(2, actorFinal);
                s3.setInt(3, alunoId);
                s3.executeUpdate();
            } catch (SQLException ignore) {}

            try (PreparedStatement s4 = conn.prepareStatement(
                "UPDATE aulas SET status='DESMARCADA' WHERE aluno_id=? AND data_aula > NOW() AND status IN ('MARCADA','AGENDADA')")) {
                s4.setInt(1, alunoId);
                desmarcadas = s4.executeUpdate();
            }

            conn.commit();
            return desmarcadas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar aluno: " + e.getMessage());
        }
    }

    public void ativar(Integer alunoId, String motivo, String actor) {
        String actorFinal = (actor == null || actor.isBlank()) ? "system" : actor;
        String motivoFinal = (motivo == null || motivo.isBlank()) ? null : motivo.trim();

        try (Connection conn = factory.getConnection()) {
            conn.setAutoCommit(false);

            String statusAtual = "ATIVO";
            try (PreparedStatement s0 = conn.prepareStatement("SELECT status FROM alunos WHERE id = ?")) {
                s0.setInt(1, alunoId);
                try (ResultSet rs = s0.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Aluno não encontrado.");
                    statusAtual = rs.getString(1);
                    if (statusAtual == null || statusAtual.isBlank()) statusAtual = "ATIVO";
                }
            }

            try (PreparedStatement s1 = conn.prepareStatement("UPDATE alunos SET status='ATIVO' WHERE id=?")) {
                s1.setInt(1, alunoId);
                s1.executeUpdate();
            }

            try (PreparedStatement s2 = conn.prepareStatement(
                    "INSERT INTO alunos_historico(aluno_id, from_status, to_status, motivo, actor) VALUES (?,?,?,?,?)")) {
                s2.setInt(1, alunoId);
                s2.setString(2, statusAtual);
                s2.setString(3, "ATIVO");
                s2.setString(4, motivoFinal);
                s2.setString(5, actorFinal);
                s2.executeUpdate();
            } catch (SQLException ignore) {}

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ativar aluno: " + e.getMessage());
        }
    }

    // =========================================================
    // CRUD básico já esperado em outros pontos
    // =========================================================
    public void inserir(Aluno aluno) {
        String sql = "INSERT INTO alunos (nome, cpf, telefone, email, data_nascimento, categoria_desejada, data_matricula, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String status = aluno.getStatus();
        if (status == null || status.isBlank()) status = "ATIVO";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getTelefone());
            stmt.setString(4, aluno.getEmail());

            if (aluno.getDataNascimento() != null) stmt.setDate(5, Date.valueOf(aluno.getDataNascimento()));
            else stmt.setDate(5, null);

            stmt.setString(6, aluno.getCategoriaDesejada());

            if (aluno.getDataMatricula() != null) stmt.setDate(7, Date.valueOf(aluno.getDataMatricula()));
            else stmt.setDate(7, Date.valueOf(java.time.LocalDate.now()));

            stmt.setString(8, status.toUpperCase());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir aluno: " + e.getMessage());
        }
    }

    public void alterar(Aluno aluno) {
        String sql = "UPDATE alunos SET nome=?, cpf=?, telefone=?, email=?, data_nascimento=?, categoria_desejada=?, data_matricula=? WHERE id=?";

        try (Connection conn = factory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getTelefone());
            stmt.setString(4, aluno.getEmail());

            if (aluno.getDataNascimento() != null) stmt.setDate(5, Date.valueOf(aluno.getDataNascimento()));
            else stmt.setDate(5, null);

            stmt.setString(6, aluno.getCategoriaDesejada());

            if (aluno.getDataMatricula() != null) stmt.setDate(7, Date.valueOf(aluno.getDataMatricula()));
            else stmt.setDate(7, Date.valueOf(java.time.LocalDate.now()));

            stmt.setInt(8, aluno.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar aluno: " + e.getMessage());
        }
    }
}
