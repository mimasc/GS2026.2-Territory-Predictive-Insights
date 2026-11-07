package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Base das estratégias de previsão. Já valida o histórico: o sistema "se cala" (lança exceção)
 * quando há poucas leituras para uma previsão confiável.
 */
public abstract class EstrategiaPrevisao {
    public static final int MINIMO_LEITURAS = 5;

    private final String nome;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int minimoLeituras) {
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    /**
     * Valida o histórico (ordem cronológica) e devolve a previsão mais ingênua: repetir o último valor.
     * As filhas sobrescrevem, chamam super.prever(...) para validar e refinam o cálculo.
     */
    public double prever(List<Double> valores) throws DadosInvalidosException {
        if (valores == null || valores.size() < minimoLeituras) {
            int qtd = valores == null ? 0 : valores.size();
            throw new DadosInvalidosException("Histórico insuficiente para a previsão '" + nome + "': "
                    + qtd + " leitura(s), mínimo de " + minimoLeituras + ". Registre mais leituras antes de prever.");
        }
        return valores.get(valores.size() - 1);
    }

    /** Explica em uma frase como a previsão é calculada. */
    public abstract String descrever();

    public String getNome() {
        return nome;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }

    protected static double media(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    protected static void validarJanela(int janela) throws DadosInvalidosException {
        if (janela < 1 || janela > 10) {
            throw new DadosInvalidosException("A janela da previsão deve estar entre 1 e 10 leituras: " + janela);
        }
    }
}
