package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Limiar;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.time.LocalDateTime;
import java.util.List;

/** Regras do sistema: ler e gravar medições, prever a partir do histórico e emitir alertas explicados. */
public class ServicoMonitoramento {
    public static final int LIMITE_HISTORICO = 50;

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final DadosTerritorio dados;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO, DadosTerritorio dados) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.dados = dados;
    }

    /** Lê o sensor, valida a medição e a grava. Nada inválido chega ao banco. */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valor = sensor.ler();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor);
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    /** Prevê o próximo valor do sensor com base no histórico do banco e emite alerta se passar de um limiar. */
    public ResultadoPrevisao avaliarSensor(Sensor sensor, EstrategiaPrevisao estrategia)
            throws DadosInvalidosException, FonteIndisponivelException {
        List<Double> valores = medicaoDAO.buscarPorSensor(sensor.getId(), LIMITE_HISTORICO).stream()
                .map(Medicao::getValor)
                .toList();
        TipoSensor tipo = sensor.getTipo();
        // A projeção não pode passar do máximo físico do sensor (ex.: umidade > 100%).
        double previsto = Math.min(estrategia.prever(valores), tipo.getMaximo());
        NivelAlerta nivel = dados.classificar(tipo, previsto);

        if (nivel == NivelAlerta.NENHUM) {
            String interpretacao = tipo.formatar(previsto) + " previstos: abaixo do limite de ATENÇÃO ("
                    + tipo.formatar(dados.getLimiar(tipo).getAtencao()) + "). Sem risco indicado pelos limiares atuais.";
            return new ResultadoPrevisao(sensor.getId(), tipo, previsto, estrategia.getNome(), nivel, interpretacao, null);
        }

        Limiar limiar = dados.getLimiar(tipo);
        double limite = limiar.valorDe(nivel);
        String explicacao = montarExplicacao(sensor, estrategia, previsto, nivel, limite);
        Alerta alerta = new Alerta(sensor.getId(), tipo, nivel, previsto, limite, estrategia.getNome(),
                explicacao, LocalDateTime.now());
        alertaDAO.salvar(alerta);
        return new ResultadoPrevisao(sensor.getId(), tipo, previsto, estrategia.getNome(), nivel, explicacao, alerta);
    }

    private String montarExplicacao(Sensor sensor, EstrategiaPrevisao estrategia, double previsto,
                                    NivelAlerta nivel, double limite) {
        TipoSensor tipo = sensor.getTipo();
        return String.format("Sensor %s (%s): previsão de %s (%s) atinge o limite de %s (%s). "
                        + "Cerca de %d pessoas na região %s podem ser afetadas. "
                        + "Este alerta apoia a decisão: confirme as condições em campo antes de agir.",
                sensor.getId(), tipo.getDescricao(), tipo.formatar(previsto), estrategia.descrever(),
                nivel.getRotulo(), tipo.formatar(limite), dados.getPopulacaoExposta(), dados.getRegiao());
    }

    public List<Medicao> historico(String sensorId, int limite) throws FonteIndisponivelException {
        return medicaoDAO.buscarPorSensor(sensorId, limite);
    }

    public List<Alerta> alertasRecentes(int limite) throws FonteIndisponivelException {
        return alertaDAO.listarRecentes(limite);
    }

    public int totalMedicoes(String sensorId) throws FonteIndisponivelException {
        return medicaoDAO.contarPorSensor(sensorId);
    }
}
