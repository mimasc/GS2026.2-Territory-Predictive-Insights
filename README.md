# Territory Predictive Insights (Turma de Fevereiro) — GS 2º/2026 · POO

Recorte escolhido: **clima local — chuva intensa e risco de alagamento** em um ponto vulnerável (margem de córrego).
O front do tema (app Flutter "Radar do Território") está em outro repositório da equipe e serviu de inspiração
para os limiares de chuva; este projeto é o backend em Java (console + JDBC + H2).

## Público beneficiado e decisão apoiada
Moradores da região de várzea (dado agregado: "população exposta", nunca pessoas individuais) e a associação
de bairro/defesa civil local. O sistema apoia a decisão **"vale a pena avisar e mobilizar a comunidade hoje?"**
(retirar móveis do térreo, abrir um ponto de apoio, limpar bueiros), prevendo o valor dos próximos minutos/horas
a partir do histórico de pluviômetro, nível do rio e umidade do solo.

## Como rodar
Requisitos: **Java 21** e **Maven 3.9+** (com `JAVA_HOME` apontando para o Java 21).

```bash
mvn test                      # 20 testes unitários
mvn -q compile exec:java      # abre o menu no console
```
O banco é **H2 em arquivo** (`./data/territorio`), criado automaticamente; o histórico sobrevive ao reiniciar.
Para zerar o histórico, apague a pasta `data/`.

Roteiro rápido de demonstração: opção `2` (8 rodadas, cenário de tempestade = `s`) → `3` (previsões e alertas)
→ `4` (histórico/alertas/CSV) → `6` (status completo). Opção `7` simula falha de sensor.

## Estrutura
```
dominio/      ComponenteTerritorial, PontoMonitorado, Sensor (interface), SensorSimulado,
              PluviometroSensor, NivelRioSensor, UmidadeSoloSensor, DadosTerritorio, Limiar,
              EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao, Medicao, Alerta, NivelAlerta
excecao/      DadosInvalidosException, FonteIndisponivelException
persistencia/ Conexao, MedicaoDAO, AlertaDAO (JDBC, PreparedStatement, try-with-resources)
servico/      ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema, RelatorioBuilder, ExportadorCsv
SistemaMonitoramento.java   menu principal
```

## Padrões criacionais (e por que ali)
- **Factory — `FabricaPrevisao`:** o menu/configuração só conhece uma chave (`"media"`, `"comparacao"`). A fábrica
  guarda um mapa chave → construtor, então escolher a estratégia não exige `if tipo ==` e adicionar uma nova
  estratégia é uma linha. É o ponto onde a variação de criação realmente existe.
- **Singleton — `ConfiguracaoSistema`:** o território e seus limiares precisam ser os mesmos para o menu e
  para o serviço; uma instância única evita duas configurações divergentes. Construtor privado, `getInstance()` sincronizado.
  (O serviço recebe o `DadosTerritorio` por construtor, o que mantém os testes independentes do singleton.)
- **Builder — `RelatorioBuilder`:** o relatório de status tem várias seções opcionais; o builder monta passo a passo
  e só gera o texto no `build()`.

## Decisões da equipe
- Sensores simulados com `Random` e um "cenário de tempestade" para a demonstração gerar tendências de alta.
- Exceções **checadas**: o compilador obriga o menu a tratar `DadosInvalidosException` e `FonteIndisponivelException`.
- Falhas do banco são encapsuladas em `FonteIndisponivelException` (o menu mostra mensagem clara, nunca um stack trace).
- A previsão é limitada ao máximo físico do sensor (umidade não passa de 100%).
- Bônus escolhido: **exportar histórico para CSV** (menu 4 → opção 3).

## Reflexão crítica
**Quando o sistema deve se calar?** Com menos de 5 leituras no banco a previsão não é confiável: o sistema recusa
prever (`DadosInvalidosException: Histórico insuficiente...`) em vez de emitir um alerta sem base. Também deve se calar
(ou avisar claramente) quando o sensor está fora de operação: nesse caso nenhuma medição é gravada.

**O que os dados não representam?** Um pluviômetro, um medidor de nível e um sensor de solo não descrevem cada rua da
várzea: o alagamento depende de drenagem, lixo em bueiros, relevo e chuva a montante, que o sistema não vê. Os dados
são simulados e a média móvel/comparação ignoram sazonalidade. A população exposta é um número agregado e estimado.

**Por que o alerta apoia a decisão em vez de decidir?** Quem conhece o território (moradores, defesa civil) tem
contexto que o sistema não tem. Cada alerta explica o sensor, o valor previsto, o método e o limite estourado, para a
pessoa avaliar e confirmar em campo; o texto do alerta diz isso explicitamente. Um falso alarme automático
ou uma omissão têm custos reais, e a responsabilidade precisa ficar com uma pessoa.

## Uso de IA
Este projeto foi desenvolvido com apoio do **Claude Code (Anthropic)**, que gerou a estrutura inicial e o código-base
a partir do enunciado, e depois foi executado (`mvn test` e rodando o menu com dados simulados) para verificação.
*Complete aqui, como equipe: o que cada integrante revisou e entendeu, e o que foi alterado por vocês.*

## Divisão de tarefas
*Preencher com os 3 integrantes (domínio/exceções · persistência · previsões/alertas/testes).*
