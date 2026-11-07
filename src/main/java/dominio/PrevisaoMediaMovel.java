package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/** Previsão = média das últimas N leituras. Suaviza ruído, mas reage devagar a subidas rápidas. */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {
    private final int janela;

    public PrevisaoMediaMovel(int janela) throws DadosInvalidosException {
        super("Média móvel", MINIMO_LEITURAS);
        validarJanela(janela);
        this.janela = janela;
    }

    @Override
    public double prever(List<Double> valores) throws DadosInvalidosException {
        super.prever(valores); // valida o histórico
        List<Double> ultimas = valores.subList(valores.size() - Math.min(janela, valores.size()), valores.size());
        return media(ultimas);
    }

    @Override
    public String descrever() {
        return "média das últimas " + janela + " leituras do histórico";
    }
}
