package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {
    private final Conexao conexao;

    public AlertaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    public void salvar(Alerta alerta) throws FonteIndisponivelException {
        String sql = "INSERT INTO alerta (sensor_id, tipo, nivel, valor_previsto, limite, estrategia, explicacao, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexao.abrir(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipo().name());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimite());
            ps.setString(6, alerta.getEstrategia());
            ps.setString(7, alerta.getExplicacao());
            ps.setTimestamp(8, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar o alerta no banco: " + e.getMessage(), e);
        }
    }

    /** Alertas já emitidos, do mais recente para o mais antigo. */
    public List<Alerta> listarRecentes(int limite) throws FonteIndisponivelException {
        String sql = "SELECT sensor_id, tipo, nivel, valor_previsto, limite, estrategia, explicacao, data_hora "
                + "FROM alerta ORDER BY id DESC LIMIT ?";
        List<Alerta> resultado = new ArrayList<>();
        try (Connection conn = conexao.abrir(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Alerta(
                            rs.getString("sensor_id"),
                            TipoSensor.valueOf(rs.getString("tipo")),
                            NivelAlerta.valueOf(rs.getString("nivel")),
                            rs.getDouble("valor_previsto"),
                            rs.getDouble("limite"),
                            rs.getString("estrategia"),
                            rs.getString("explicacao"),
                            rs.getTimestamp("data_hora").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar alertas no banco: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new FonteIndisponivelException("Registro de alerta inconsistente no banco: " + e.getMessage(), e);
        }
        return resultado;
    }
}
