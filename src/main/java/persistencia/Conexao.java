package persistencia;

import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Abre conexões com o H2 (em arquivo, para o histórico sobreviver ao reiniciar) e cria as tabelas. */
public class Conexao {
    public static final String URL_PADRAO = "jdbc:h2:file:./data/territorio";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private final String url;

    public Conexao() {
        this(URL_PADRAO);
    }

    public Conexao(String url) {
        this.url = url;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, USUARIO, SENHA);
    }

    public void inicializar() throws FonteIndisponivelException {
        String tabelaMedicao = """
                CREATE TABLE IF NOT EXISTS medicao (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    sensor_id VARCHAR(40) NOT NULL,
                    tipo VARCHAR(20) NOT NULL,
                    valor DOUBLE NOT NULL,
                    data_hora TIMESTAMP NOT NULL
                )""";
        String tabelaAlerta = """
                CREATE TABLE IF NOT EXISTS alerta (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    sensor_id VARCHAR(40) NOT NULL,
                    tipo VARCHAR(20) NOT NULL,
                    nivel VARCHAR(10) NOT NULL,
                    valor_previsto DOUBLE NOT NULL,
                    limite DOUBLE NOT NULL,
                    estrategia VARCHAR(60) NOT NULL,
                    explicacao VARCHAR(600) NOT NULL,
                    data_hora TIMESTAMP NOT NULL
                )""";
        try (Connection conn = abrir(); Statement st = conn.createStatement()) {
            st.execute(tabelaMedicao);
            st.execute(tabelaAlerta);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Não foi possível preparar o banco de dados: " + e.getMessage(), e);
        }
    }
}
