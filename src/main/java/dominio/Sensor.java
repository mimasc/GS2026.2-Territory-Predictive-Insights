package dominio;

import excecao.FonteIndisponivelException;

public interface Sensor {
    String getId();

    TipoSensor getTipo();

    /** Lê o valor atual. Lança FonteIndisponivelException se o sensor não estiver respondendo. */
    double ler() throws FonteIndisponivelException;

    boolean estaFuncionando();
}
