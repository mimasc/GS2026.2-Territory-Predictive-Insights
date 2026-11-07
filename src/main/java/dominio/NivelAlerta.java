package dominio;

/** Níveis em ordem crescente de gravidade (a ordem do enum é usada em comparações). */
public enum NivelAlerta {
    NENHUM("NORMAL"),
    ATENCAO("ATENÇÃO"),
    ALERTA("ALERTA"),
    CRITICO("CRÍTICO");

    private final String rotulo;

    NivelAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
