package servico;

import dominio.Medicao;
import excecao.FonteIndisponivelException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Bônus: exporta o histórico consultado para CSV. */
public class ExportadorCsv {
    public void exportar(List<Medicao> medicoes, Path destino) throws FonteIndisponivelException {
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write("sensor_id,tipo,valor,unidade,data_hora");
            w.newLine();
            for (Medicao m : medicoes) {
                w.write(String.join(",", m.getSensorId(), m.getTipo().name(),
                        String.valueOf(m.getValor()), m.getTipo().getUnidade(), m.getDataHora().withNano(0).toString()));
                w.newLine();
            }
        } catch (IOException e) {
            throw new FonteIndisponivelException("Não foi possível gravar o arquivo " + destino + ": " + e.getMessage(), e);
        }
    }
}
