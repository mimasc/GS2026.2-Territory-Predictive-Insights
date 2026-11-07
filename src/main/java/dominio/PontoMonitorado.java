package dominio;

import java.util.List;

/** Ponto vulnerável do território (ex.: margem de córrego) que agrupa sensores. */
public class PontoMonitorado extends ComponenteTerritorial {
    private final List<Sensor> sensores;

    public PontoMonitorado(String id, String nome, String regiao, List<Sensor> sensores) {
        super(id, nome, regiao);
        this.sensores = List.copyOf(sensores);
    }

    public List<Sensor> getSensores() {
        return sensores;
    }

    @Override
    public StatusComponente verificarStatus() {
        long ativos = sensores.stream().filter(Sensor::estaFuncionando).count();
        StatusComponente novo;
        if (ativos == sensores.size()) {
            novo = StatusComponente.OPERACIONAL;
        } else if (ativos == 0) {
            novo = StatusComponente.FORA_DO_AR;
        } else {
            novo = StatusComponente.DEGRADADO;
        }
        setStatus(novo);
        return novo;
    }
}
