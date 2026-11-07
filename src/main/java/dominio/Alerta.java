package dominio;

import java.time.LocalDateTime;

/** Alerta emitido a partir de uma previsão que ultrapassou um limiar. */
public class Alerta {
    private final String sensorId;
    private final TipoSensor tipo;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limite;
    private final String estrategia;
    private final String explicacao;
    private final LocalDateTime dataHora;

    public Alerta(String sensorId, TipoSensor tipo, NivelAlerta nivel, double valorPrevisto, double limite,
                  String estrategia, String explicacao, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limite = limite;
        this.estrategia = estrategia;
        this.explicacao = explicacao;
        this.dataHora = dataHora;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimite() {
        return limite;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora.withNano(0) + " | [" + nivel.getRotulo() + "] " + sensorId + " | " + explicacao;
    }
}
