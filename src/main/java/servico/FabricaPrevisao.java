package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory: cria a estratégia a partir de uma chave de configuração, sem cadeia de if/switch.
 * Para criar uma nova estratégia basta registrar uma linha no construtor.
 */
public class FabricaPrevisao {
    @FunctionalInterface
    private interface Criador {
        EstrategiaPrevisao criar(int janela) throws DadosInvalidosException;
    }

    private final Map<String, Criador> criadores = new LinkedHashMap<>();

    public FabricaPrevisao() {
        criadores.put("media", PrevisaoMediaMovel::new);
        criadores.put("comparacao", PrevisaoComparacao::new);
    }

    public EstrategiaPrevisao criar(String chave, int janela) throws DadosInvalidosException {
        Criador criador = chave == null ? null : criadores.get(chave.toLowerCase());
        if (criador == null) {
            throw new DadosInvalidosException("Estratégia de previsão desconhecida: '" + chave
                    + "'. Disponíveis: " + criadores.keySet());
        }
        return criador.criar(janela);
    }

    public Set<String> chavesDisponiveis() {
        return criadores.keySet();
    }
}
