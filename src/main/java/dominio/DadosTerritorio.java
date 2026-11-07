package dominio;

import excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;

/**
 * Dados agregados do território (sem nenhum dado pessoal) e seus limiares de alerta.
 * Os atributos são privados: toda alteração passa por setters que validam.
 */
public class DadosTerritorio {
    private String regiao;
    private int populacaoExposta;
    private final Map<TipoSensor, Limiar> limiares = new EnumMap<>(TipoSensor.class);

    public DadosTerritorio(String regiao, int populacaoExposta) throws DadosInvalidosException {
        setRegiao(regiao);
        setPopulacaoExposta(populacaoExposta);
        // Valores iniciais, inspirados nas regras de chuva do front (Radar do Território).
        setLimiar(TipoSensor.CHUVA, 15, 30, 50);
        setLimiar(TipoSensor.NIVEL_RIO, 2.5, 3.5, 4.5);
        setLimiar(TipoSensor.UMIDADE_SOLO, 70, 85, 95);
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("O nome da região não pode ser vazio.");
        }
        this.regiao = regiao.trim();
    }

    public int getPopulacaoExposta() {
        return populacaoExposta;
    }

    public void setPopulacaoExposta(int populacaoExposta) throws DadosInvalidosException {
        if (populacaoExposta < 0) {
            throw new DadosInvalidosException("População exposta não pode ser negativa: " + populacaoExposta);
        }
        this.populacaoExposta = populacaoExposta;
    }

    public Limiar getLimiar(TipoSensor tipo) {
        return limiares.get(tipo);
    }

    /** Altera os limiares de um tipo de sensor; o estado anterior é mantido se os novos forem inválidos. */
    public void setLimiar(TipoSensor tipo, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        limiares.put(tipo, new Limiar(tipo, atencao, alerta, critico));
    }

    /** Classifica um valor (lido ou previsto) no maior nível cujo limite foi atingido. */
    public NivelAlerta classificar(TipoSensor tipo, double valor) {
        Limiar limiar = limiares.get(tipo);
        if (valor >= limiar.getCritico()) {
            return NivelAlerta.CRITICO;
        }
        if (valor >= limiar.getAlerta()) {
            return NivelAlerta.ALERTA;
        }
        if (valor >= limiar.getAtencao()) {
            return NivelAlerta.ATENCAO;
        }
        return NivelAlerta.NENHUM;
    }
}
