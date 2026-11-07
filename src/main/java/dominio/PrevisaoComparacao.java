package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Compara a média das últimas N leituras com a do período anterior e projeta a mesma variação
 * para o próximo período. Reage mais rápido a tendências que a média móvel.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {
    private final int janela;

    public PrevisaoComparacao(int janela) throws DadosInvalidosException {
        super("Comparação com período anterior", MINIMO_LEITURAS);
        validarJanela(janela);
        this.janela = janela;
    }

    @Override
    public double prever(List<Double> valores) throws DadosInvalidosException {
        super.prever(valores); // valida o histórico
        int n = valores.size();
        int tamanhoRecente = Math.min(janela, n - 1);
        List<Double> recente = valores.subList(n - tamanhoRecente, n);
        int inicioAnterior = Math.max(0, n - tamanhoRecente - janela);
        List<Double> anterior = valores.subList(inicioAnterior, n - tamanhoRecente);
        double mediaRecente = media(recente);
        double variacao = mediaRecente - media(anterior);
        return Math.max(0.0, mediaRecente + variacao);
    }

    @Override
    public String descrever() {
        return "média das últimas " + janela + " leituras somada à variação em relação ao período anterior";
    }
}
