package dominio;

import excecao.DadosInvalidosException;

/** Trio de limites (atenção < alerta < crítico) de um tipo de sensor. Imutável e sempre válido. */
public class Limiar {
    private final double atencao;
    private final double alerta;
    private final double critico;

    public Limiar(TipoSensor tipo, double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (Double.isNaN(atencao) || Double.isNaN(alerta) || Double.isNaN(critico)) {
            throw new DadosInvalidosException("Limiar de " + tipo.getDescricao() + " contém valor não numérico.");
        }
        if (atencao <= tipo.getMinimo()) {
            throw new DadosInvalidosException("Limiar de ATENÇÃO de " + tipo.getDescricao()
                    + " deve ser maior que " + tipo.formatar(tipo.getMinimo()) + ".");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException("Limiares de " + tipo.getDescricao()
                    + " devem ser crescentes (atenção < alerta < crítico): " + atencao + " / " + alerta + " / " + critico);
        }
        if (critico > tipo.getMaximo()) {
            throw new DadosInvalidosException("Limiar CRÍTICO de " + tipo.getDescricao()
                    + " acima do máximo possível do sensor (" + tipo.formatar(tipo.getMaximo()) + ").");
        }
        this.atencao = atencao;
        this.alerta = alerta;
        this.critico = critico;
    }

    public double getAtencao() {
        return atencao;
    }

    public double getAlerta() {
        return alerta;
    }

    public double getCritico() {
        return critico;
    }

    /** Limite correspondente a um nível. NENHUM não tem limite. */
    public double valorDe(NivelAlerta nivel) {
        return switch (nivel) {
            case ATENCAO -> atencao;
            case ALERTA -> alerta;
            case CRITICO -> critico;
            case NENHUM -> throw new IllegalArgumentException("O nível NENHUM não possui limite.");
        };
    }
}
