package servico;

import dominio.DadosTerritorio;
import excecao.DadosInvalidosException;

/**
 * Singleton: existe uma única configuração (território, limiares, estratégia e janela) para todo o
 * sistema. Assim o menu e o serviço enxergam sempre os mesmos limiares, sem passar objetos por toda parte.
 */
public final class ConfiguracaoSistema {
    private static ConfiguracaoSistema instancia;

    private final DadosTerritorio dadosTerritorio;
    private String estrategiaPadrao = "media";
    private int janela = 3;

    private ConfiguracaoSistema() {
        try {
            this.dadosTerritorio = new DadosTerritorio("Várzea do Córrego (exemplo)", 1200);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException("Configuração padrão do território é inválida", e);
        }
    }

    public static synchronized ConfiguracaoSistema getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    public String getEstrategiaPadrao() {
        return estrategiaPadrao;
    }

    public void setEstrategiaPadrao(String estrategiaPadrao) {
        this.estrategiaPadrao = estrategiaPadrao;
    }

    public int getJanela() {
        return janela;
    }

    public void setJanela(int janela) throws DadosInvalidosException {
        if (janela < 1 || janela > 10) {
            throw new DadosInvalidosException("A janela deve estar entre 1 e 10 leituras: " + janela);
        }
        this.janela = janela;
    }
}
