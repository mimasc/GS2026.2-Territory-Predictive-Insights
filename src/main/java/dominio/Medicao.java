package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;

/** Uma leitura de sensor. Só existe se o valor estiver dentro da faixa física do tipo. */
public class Medicao {
    private final String sensorId;
    private final TipoSensor tipo;
    private final double valor;
    private final LocalDateTime dataHora;

    public Medicao(String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("Medição sem identificação de sensor.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("Medição sem tipo de sensor.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Medição sem data/hora.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new DadosInvalidosException("Medição de " + tipo.getDescricao() + " não é um número válido.");
        }
        if (valor < tipo.getMinimo()) {
            throw new DadosInvalidosException(
                    "Medição de " + tipo.getDescricao() + " negativa/abaixo do mínimo: " + tipo.formatar(valor));
        }
        if (valor > tipo.getMaximo()) {
            throw new DadosInvalidosException("Medição de " + tipo.getDescricao()
                    + " acima do máximo possível (" + tipo.formatar(tipo.getMaximo()) + "): " + tipo.formatar(valor));
        }
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Medicao(String sensorId, TipoSensor tipo, double valor) throws DadosInvalidosException {
        this(sensorId, tipo, valor, LocalDateTime.now());
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora.withNano(0) + " | " + sensorId + " | " + tipo.formatar(valor);
    }
}
