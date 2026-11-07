package servico;

import java.util.ArrayList;
import java.util.List;

/** Builder: monta o relatório de status passo a passo (título, seções, rodapé) e só então gera o texto. */
public class RelatorioBuilder {
    private String titulo = "Relatório";
    private final List<String> secoes = new ArrayList<>();
    private String rodape = "";

    public RelatorioBuilder titulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public RelatorioBuilder secao(String nome, List<String> linhas) {
        StringBuilder sb = new StringBuilder("\n-- ").append(nome).append(" --\n");
        if (linhas.isEmpty()) {
            sb.append("  (nada a exibir)\n");
        }
        for (String linha : linhas) {
            sb.append("  ").append(linha).append('\n');
        }
        secoes.add(sb.toString());
        return this;
    }

    public RelatorioBuilder rodape(String rodape) {
        this.rodape = rodape;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder("==== ").append(titulo).append(" ====\n");
        secoes.forEach(sb::append);
        if (!rodape.isBlank()) {
            sb.append('\n').append(rodape).append('\n');
        }
        return sb.toString();
    }
}
