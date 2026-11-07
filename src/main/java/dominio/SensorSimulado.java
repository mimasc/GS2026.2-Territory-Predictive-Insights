package dominio;

import excecao.FonteIndisponivelException;

import java.util.Random;

/** Base dos sensores simulados: mantém o último valor e deixa cada tipo definir como ele evolui. */
public abstract class SensorSimulado implements Sensor {
    private final String id;
    private final TipoSensor tipo;
    protected final Random random;
    private double ultimoValor;
    private boolean funcionando = true;
    private boolean cenarioTempestade = false;

    protected SensorSimulado(String id, TipoSensor tipo, Random random, double valorInicial) {
        this.id = id;
        this.tipo = tipo;
        this.random = random;
        this.ultimoValor = valorInicial;
    }

    /** Calcula o próximo valor a partir do anterior (sem se preocupar com a faixa; ela é ajustada depois). */
    protected abstract double proximoValor(double anterior, boolean tempestade);

    @Override
    public double ler() throws FonteIndisponivelException {
        if (!funcionando) {
            throw new FonteIndisponivelException("Sensor " + id + " (" + tipo.getDescricao() + ") está fora de operação.");
        }
        double bruto = proximoValor(ultimoValor, cenarioTempestade);
        double limitado = Math.min(tipo.getMaximo(), Math.max(tipo.getMinimo(), bruto));
        ultimoValor = Math.round(limitado * 10.0) / 10.0;
        return ultimoValor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return tipo;
    }

    @Override
    public boolean estaFuncionando() {
        return funcionando;
    }

    public void setFuncionando(boolean funcionando) {
        this.funcionando = funcionando;
    }

    public void setCenarioTempestade(boolean cenarioTempestade) {
        this.cenarioTempestade = cenarioTempestade;
    }
}
