package dominio;

import java.util.Locale;

/** Tipos de sensor do recorte "chuva intensa e risco de alagamento", com a faixa física válida. */
public enum TipoSensor {
    CHUVA("Pluviômetro", "mm/h", 0, 300),
    NIVEL_RIO("Nível do rio", "m", 0, 15),
    UMIDADE_SOLO("Umidade do solo", "%", 0, 100);

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final String descricao;
    private final String unidade;
    private final double minimo;
    private final double maximo;

    TipoSensor(String descricao, String unidade, double minimo, double maximo) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    /** Formata um valor com a unidade do tipo, ex.: "12,3 mm/h". */
    public String formatar(double valor) {
        return String.format(PT_BR, "%.1f %s", valor, unidade);
    }
}
