package servico;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;

/** Resultado de uma previsão: o valor, o que ele significa e o alerta emitido (nulo se estiver normal). */
public record ResultadoPrevisao(String sensorId, TipoSensor tipo, double valorPrevisto, String estrategia,
                                NivelAlerta nivel, String interpretacao, Alerta alerta) {
    public boolean temAlerta() {
        return alerta != null;
    }
}
