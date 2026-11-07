import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Medicao;
import dominio.NivelRioSensor;
import dominio.PluviometroSensor;
import dominio.PontoMonitorado;
import dominio.Sensor;
import dominio.SensorSimulado;
import dominio.TipoSensor;
import dominio.UmidadeSoloSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.Conexao;
import persistencia.MedicaoDAO;
import servico.ConfiguracaoSistema;
import servico.ExportadorCsv;
import servico.FabricaPrevisao;
import servico.RelatorioBuilder;
import servico.ResultadoPrevisao;
import servico.ServicoMonitoramento;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Menu principal (console). Aqui as exceções são capturadas e viram mensagens claras. */
public class SistemaMonitoramento {
    private final Scanner entrada;
    private final ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();
    private final DadosTerritorio dados = config.getDadosTerritorio();
    private final FabricaPrevisao fabrica = new FabricaPrevisao();
    private final List<SensorSimulado> sensores;
    private final PontoMonitorado ponto;
    private final ServicoMonitoramento servico;

    public SistemaMonitoramento(Conexao conexao, Scanner entrada) {
        this.entrada = entrada;
        Random random = new Random();
        this.sensores = List.of(
                new PluviometroSensor("PLUV-01", random),
                new NivelRioSensor("RIO-01", random),
                new UmidadeSoloSensor("SOLO-01", random));
        this.ponto = new PontoMonitorado("PM-01", "Margem do córrego", dados.getRegiao(), new ArrayList<Sensor>(sensores));
        this.servico = new ServicoMonitoramento(new MedicaoDAO(conexao), new AlertaDAO(conexao), dados);
    }

    public static void main(String[] args) {
        Conexao conexao = new Conexao();
        try {
            conexao.inicializar();
        } catch (FonteIndisponivelException e) {
            System.err.println("Não foi possível iniciar o sistema: " + e.getMessage());
            return;
        }
        new SistemaMonitoramento(conexao, new Scanner(System.in)).executar();
    }

    public void executar() {
        System.out.println("=== Territory Predictive Insights — chuva e alagamento ===");
        try {
            while (true) {
                exibirMenu();
                int opcao = lerInt("Escolha: ");
                if (opcao == 0) {
                    break;
                }
                executarOpcao(opcao);
            }
        } catch (IllegalStateException e) {
            System.out.println("\nEntrada encerrada.");
        }
        System.out.println("Até logo! O histórico foi mantido no banco.");
    }

    private void exibirMenu() {
        System.out.println("""

                1 - Verificar sensores (uma leitura de cada)
                2 - Simular lote de leituras
                3 - Rodar previsões e alertas
                4 - Ver histórico (medições, alertas, CSV)
                5 - Gerenciar dados do território
                6 - Status completo
                7 - Ligar/desligar sensor (simular falha)
                0 - Sair""");
    }

    private void executarOpcao(int opcao) {
        try {
            switch (opcao) {
                case 1 -> verificarSensores();
                case 2 -> simularLote();
                case 3 -> rodarPrevisoes();
                case 4 -> menuHistorico();
                case 5 -> menuTerritorio();
                case 6 -> statusCompleto();
                case 7 -> alternarSensor();
                default -> System.out.println("Opção inválida: " + opcao);
            }
        } catch (DadosInvalidosException e) {
            System.out.println("Dado inválido: " + e.getMessage());
        } catch (FonteIndisponivelException e) {
            System.out.println("Fonte indisponível: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 1, 2

    private void verificarSensores() {
        for (Sensor s : sensores) {
            lerEGravar(s);
        }
    }

    private void lerEGravar(Sensor sensor) {
        try {
            Medicao m = servico.registrarLeitura(sensor);
            System.out.println("  " + m);
        } catch (FonteIndisponivelException e) {
            System.out.println("  [" + sensor.getId() + "] sem leitura: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("  [" + sensor.getId() + "] leitura descartada: " + e.getMessage());
        }
    }

    private void simularLote() {
        int rodadas = lerInt("Quantas rodadas de leitura (1 a 50)? ");
        if (rodadas < 1 || rodadas > 50) {
            System.out.println("Informe um número de rodadas entre 1 e 50.");
            return;
        }
        boolean tempestade = lerSimNao("Simular cenário de tempestade (valores subindo)? (s/n) ");
        sensores.forEach(s -> s.setCenarioTempestade(tempestade));
        for (int i = 0; i < rodadas; i++) {
            for (Sensor s : sensores) {
                lerEGravar(s);
            }
        }
        sensores.forEach(s -> s.setCenarioTempestade(false));
        System.out.println(rodadas + " rodada(s) registrada(s).");
    }

    // ---------------------------------------------------------------- 3

    private void rodarPrevisoes() throws DadosInvalidosException {
        System.out.println("Estratégias: " + fabrica.chavesDisponiveis() + " (padrão: " + config.getEstrategiaPadrao() + ")");
        System.out.print("Estratégia (Enter = padrão): ");
        String chave = entrada.nextLine().trim();
        if (chave.isEmpty()) {
            chave = config.getEstrategiaPadrao();
        }
        EstrategiaPrevisao estrategia = fabrica.criar(chave, config.getJanela());
        System.out.println("Usando: " + estrategia.getNome() + " — " + estrategia.descrever());
        for (Sensor s : sensores) {
            try {
                ResultadoPrevisao r = servico.avaliarSensor(s, estrategia);
                System.out.println("\n[" + s.getId() + "] previsto: " + r.tipo().formatar(r.valorPrevisto())
                        + " -> " + r.nivel().getRotulo());
                System.out.println("  " + r.interpretacao());
            } catch (DadosInvalidosException e) {
                System.out.println("\n[" + s.getId() + "] sem previsão: " + e.getMessage());
            } catch (FonteIndisponivelException e) {
                System.out.println("\n[" + s.getId() + "] falha na consulta: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- 4

    private void menuHistorico() throws FonteIndisponivelException {
        System.out.println("\n1 - Medições de um sensor | 2 - Alertas emitidos | 3 - Exportar sensor para CSV | 0 - Voltar");
        int opcao = lerInt("Escolha: ");
        switch (opcao) {
            case 1 -> {
                Sensor s = escolherSensor();
                if (s != null) {
                    List<Medicao> lista = servico.historico(s.getId(), 20);
                    System.out.println("Últimas " + lista.size() + " medições de " + s.getId() + ":");
                    lista.forEach(m -> System.out.println("  " + m));
                }
            }
            case 2 -> {
                List<Alerta> alertas = servico.alertasRecentes(20);
                System.out.println(alertas.size() + " alerta(s) mais recente(s):");
                alertas.forEach(a -> System.out.println("  " + a));
            }
            case 3 -> {
                Sensor s = escolherSensor();
                if (s != null) {
                    Path destino = Path.of("historico-" + s.getId() + ".csv");
                    new ExportadorCsv().exportar(servico.historico(s.getId(), ServicoMonitoramento.LIMITE_HISTORICO), destino);
                    System.out.println("Arquivo gerado: " + destino.toAbsolutePath());
                }
            }
            case 0 -> { }
            default -> System.out.println("Opção inválida: " + opcao);
        }
    }

    private Sensor escolherSensor() {
        for (int i = 0; i < sensores.size(); i++) {
            Sensor s = sensores.get(i);
            System.out.println("  " + (i + 1) + " - " + s.getId() + " (" + s.getTipo().getDescricao() + ")");
        }
        int n = lerInt("Sensor: ");
        if (n < 1 || n > sensores.size()) {
            System.out.println("Sensor inexistente: " + n);
            return null;
        }
        return sensores.get(n - 1);
    }

    // ---------------------------------------------------------------- 5

    private void menuTerritorio() throws DadosInvalidosException {
        System.out.println("\nRegião: " + dados.getRegiao() + " | População exposta: " + dados.getPopulacaoExposta()
                + " | Janela de previsão: " + config.getJanela());
        for (TipoSensor t : TipoSensor.values()) {
            var l = dados.getLimiar(t);
            System.out.println("  " + t.getDescricao() + ": atenção " + t.formatar(l.getAtencao())
                    + " | alerta " + t.formatar(l.getAlerta()) + " | crítico " + t.formatar(l.getCritico()));
        }
        System.out.println("1 - Alterar região | 2 - Alterar população | 3 - Alterar limiares | 4 - Alterar janela | 0 - Voltar");
        int opcao = lerInt("Escolha: ");
        switch (opcao) {
            case 1 -> {
                System.out.print("Novo nome da região: ");
                dados.setRegiao(entrada.nextLine());
            }
            case 2 -> dados.setPopulacaoExposta(lerInt("Nova população exposta: "));
            case 3 -> alterarLimiares();
            case 4 -> config.setJanela(lerInt("Nova janela (1 a 10): "));
            case 0 -> { }
            default -> System.out.println("Opção inválida: " + opcao);
        }
        if (opcao >= 1 && opcao <= 4) {
            System.out.println("Dados atualizados.");
        }
    }

    private void alterarLimiares() throws DadosInvalidosException {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("  " + (i + 1) + " - " + tipos[i].getDescricao());
        }
        int n = lerInt("Tipo: ");
        if (n < 1 || n > tipos.length) {
            throw new DadosInvalidosException("Tipo de sensor inexistente: " + n);
        }
        TipoSensor tipo = tipos[n - 1];
        double atencao = lerDouble("Novo limite de ATENÇÃO (" + tipo.getUnidade() + "): ");
        double alerta = lerDouble("Novo limite de ALERTA (" + tipo.getUnidade() + "): ");
        double critico = lerDouble("Novo limite CRÍTICO (" + tipo.getUnidade() + "): ");
        dados.setLimiar(tipo, atencao, alerta, critico);
    }

    // ---------------------------------------------------------------- 6, 7

    private void statusCompleto() throws FonteIndisponivelException {
        List<String> sensoresLinhas = new ArrayList<>();
        for (Sensor s : sensores) {
            List<Medicao> ultima = servico.historico(s.getId(), 1);
            String leitura = ultima.isEmpty() ? "sem leituras" : "última: " + ultima.get(0);
            sensoresLinhas.add(s.getId() + " | " + (s.estaFuncionando() ? "ativo" : "FORA DE OPERAÇÃO")
                    + " | " + servico.totalMedicoes(s.getId()) + " medições | " + leitura);
        }
        List<String> alertasLinhas = servico.alertasRecentes(5).stream().map(Alerta::toString).toList();
        String relatorio = new RelatorioBuilder()
                .titulo("Status completo")
                .secao("Território", List.of("Região: " + dados.getRegiao(),
                        "População exposta: " + dados.getPopulacaoExposta()))
                .secao("Ponto " + ponto.getNome(), List.of("Status: " + ponto.verificarStatus()))
                .secao("Sensores", sensoresLinhas)
                .secao("Últimos alertas", alertasLinhas)
                .rodape("Os alertas apoiam a decisão da equipe; não substituem a avaliação em campo.")
                .build();
        System.out.println(relatorio);
    }

    private void alternarSensor() {
        Sensor s = escolherSensor();
        if (s instanceof SensorSimulado simulado) {
            simulado.setFuncionando(!simulado.estaFuncionando());
            System.out.println(s.getId() + (simulado.estaFuncionando() ? " religado." : " desligado (falha simulada)."));
        }
    }

    // ---------------------------------------------------------------- entrada

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        if (!entrada.hasNextLine()) {
            throw new IllegalStateException("Entrada encerrada");
        }
        return entrada.nextLine().trim();
    }

    private int lerInt(String mensagem) {
        while (true) {
            String texto = lerLinha(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número inteiro. Tente novamente.");
            }
        }
    }

    private double lerDouble(String mensagem) {
        while (true) {
            String texto = lerLinha(mensagem);
            try {
                return Double.parseDouble(texto.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número. Tente novamente.");
            }
        }
    }

    private boolean lerSimNao(String mensagem) {
        return lerLinha(mensagem).equalsIgnoreCase("s");
    }
}
