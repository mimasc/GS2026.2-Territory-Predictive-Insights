package persistencia;

import dominio.Medicao;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicaoDAO {
    private final Conexao conexao;

    public MedicaoDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    public void salvar(Medicao medicao) throws FonteIndisponivelException {
        String sql = "INSERT INTO medicao (sensor_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexao.abrir(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipo().name());
            ps.setDouble(3, medicao.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar a medição no banco: " + e.getMessage(), e);
        }
    }

    /** Últimas {@code limite} medições do sensor, em ordem cronológica (da mais antiga para a mais recente). */
    public List<Medicao> buscarPorSensor(String sensorId, int limite) throws FonteIndisponivelException {
        String sql = "SELECT sensor_id, tipo, valor, data_hora FROM medicao "
                + "WHERE sensor_id = ? ORDER BY id DESC LIMIT ?";
        List<Medicao> resultado = new ArrayList<>();
        try (Connection conn = conexao.abrir(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sensorId);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Medicao(
                            rs.getString("sensor_id"),
                            TipoSensor.valueOf(rs.getString("tipo")),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar o histórico no banco: " + e.getMessage(), e);
        } catch (DadosInvalidosException | IllegalArgumentException e) {
            throw new FonteIndisponivelException("Registro de medição inconsistente no banco: " + e.getMessage(), e);
        }
        Collections.reverse(resultado);
        return resultado;
    }

    public int contarPorSensor(String sensorId) throws FonteIndisponivelException {
        String sql = "SELECT COUNT(*) FROM medicao WHERE sensor_id = ?";
        try (Connection conn = conexao.abrir(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sensorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao contar medições no banco: " + e.getMessage(), e);
        }
    }
}
