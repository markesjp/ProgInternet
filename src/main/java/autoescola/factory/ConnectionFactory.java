package autoescola.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

// Esta classe é responsável por criar e fechar as conexões com o banco de dados.
public class ConnectionFactory {
    
    // 1. Configurações da Conexão
    
    // CORREÇÃO FINAL DA URL:
    // * Garante o fuso horário reconhecível pelo Java: 'serverTimezone=America/Sao_Paulo'
    // * Desativa o SSL para evitar problemas de certificado: '&useSSL=false'
    private static final String URL = "jdbc:mysql://localhost:3306/autoescola_db?serverTimezone=America/Sao_Paulo&useSSL=false&allowPublicKeyRetrieval=true";
    
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    /**
     * Devolve uma nova conexão com o banco de dados.
     * @return Connection
     */
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Lança uma exceção de tempo de execução, preservando o erro SQL original.
            throw new RuntimeException("Erro ao conectar com o banco de dados. Verifique a URL, usuário/senha E o fuso horário do servidor.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC do MySQL não encontrado no classpath.", e);
        }
    }

    /**
     * Fecha recursos com segurança. (Mesmo usando try-with-resources nos DAOs,
     * esses métodos ajudam em testes e extensões futuras.)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static void closeConnection(Connection conn, Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        closeConnection(conn);
    }

    public static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        closeConnection(conn, stmt);
    }
}