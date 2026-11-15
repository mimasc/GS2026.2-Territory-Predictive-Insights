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
Requisito: apenas o **Java 21** (com `JAVA_HOME` apontando para ele). **Não é preciso instalar o Maven**: o projeto
traz o Maven Wrapper, que baixa a versão certa do Maven na primeira execução (precisa de internet nessa vez).

```bash
# Windows
mvnw.cmd test                     # 22 testes unitários
mvnw.cmd -q compile exec:java     # abre o menu no console

# macOS / Linux
./mvnw test
./mvnw -q compile exec:java
```
> **Atenção ao Java:** use o **Java 21**; com outra versão os testes com Mockito podem falhar. Confira com
> `java -version`. Se tiver mais de um JDK: macOS `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` ·
> Windows `set JAVA_HOME=C:\caminho\do\jdk-21`. Quem já tem Maven 3.9+ instalado pode usar `mvn` no lugar de `mvnw`.

## Banco de dados (configuração para a correção)
O banco é o **H2 em arquivo** (`jdbc:h2:file:./data/territorio`, usuário `sa`, sem senha). **Não é preciso instalar
nem configurar nada**: a dependência do H2 vem pelo Maven e as tabelas `medicao` e `alerta` são criadas sozinhas na
primeira execução (`CREATE TABLE IF NOT EXISTS`, em `Conexao.inicializar()`). Como o banco é um arquivo, o histórico
**sobrevive ao reiniciar** o programa. Para zerar, feche o programa e apague a pasta `data/` (ela não vai para o Git).

## Roteiro de demonstração
1. Com o banco vazio, opção `3` → o sistema **se cala**: "Histórico insuficiente" (menos de 5 leituras).
2. Opção `2` → 8 rodadas com cenário de tempestade (`s`) → opção `3` (estratégia `media`, depois `comparacao`)
   → previsões e alertas explicados.
3. Opção `7` desliga um sensor e a opção `1` mostra a falha tratada (`FonteIndisponivelException`).
4. Opção `5` → `2` com população negativa mostra o `DadosInvalidosException` tratado.
5. Opção `6` (status completo, Builder) e opção `4` (medições por sensor, alertas, exportar CSV).
6. Feche e abra de novo: opção `4` mostra que o histórico continua no banco.

## Testes
`mvnw test` roda **22 testes JUnit 5** no padrão AAA (Arrange/Act/Assert), sem depender de banco nem de digitação:
`MedicaoTest`, `DadosTerritorioTest`, `PrevisaoMediaMovelTest`, `PrevisaoComparacaoTest`, `FabricaPrevisaoTest` e
`ServicoMonitoramentoTest`. Há **9 `assertThrows`** validando as exceções customizadas e, em
`ServicoMonitoramentoTest`, **Mockito** mocka `MedicaoDAO`, `AlertaDAO` e `Sensor` para testar a lógica de alerta
sem banco. Os cálculos são testados com valores esperados (se a conta errar, o teste fica vermelho).

## Estrutura
```
dominio/      ComponenteTerritorial, PontoMonitorado, Sensor (interface), SensorSimulado,
              PluviometroSensor, NivelRioSensor, UmidadeSoloSensor, DadosTerritorio, Limiar,
              EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao, Medicao, Alerta, NivelAlerta
excecao/      DadosInvalidosException, FonteIndisponivelException
persistencia/ Conexao, MedicaoDAO, AlertaDAO (JDBC, PreparedStatement, try-with-resources)
servico/      ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema, RelatorioBuilder, ExportadorCsv
SistemaMonitoramento.java   menu principal
src/test/java/  testes JUnit 5 + Mockito (espelham os pacotes acima)
mvnw, mvnw.cmd, .mvn/       Maven Wrapper (roda o projeto sem instalar o Maven)
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
- Alerta repetido não é gravado de novo: se o alerta é idêntico ao último do sensor (mesmo nível, estratégia e valor
  previsto) e não chegou leitura nova desde então, o sistema mostra o resultado mas não duplica o registro no
  histórico (`ServicoMonitoramento.repetido`). Leitura nova, outra estratégia ou outra janela geram alerta novo.
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
a partir do enunciado. A IA também foi usada para revisar o projeto contra os critérios de avaliação e para
explicar o código à equipe. O que a equipe fez sobre esse código:

- **Mirella:** subiu o código-base ao repositório (commit inicial) e ajustou o README (equipe de 2 integrantes e aviso
  sobre a versão do Java).
- **André:** revisou o projeto contra o enunciado; adicionou o Maven Wrapper para o projeto rodar sem Maven instalado;
  removeu o `instanceof` da opção de simular falha de sensor (o menu passou a trabalhar direto com `SensorSimulado`);
  corrigiu o registro de alertas repetidos (novo `AlertaDAO.buscarUltimoPorSensor` + regra `repetido` no serviço, com
  2 testes novos); atualizou as instruções de execução deste README.

Limitação assumida: parte do código-base não foi escrita manualmente por nós. O que afirmamos é que o executamos,
testamos, revisamos e entendemos o seu funcionamento, e cada integrante consegue explicar a sua frente.

## Divisão de tarefas
Equipe de 2 integrantes: **Mirella Mascarenhas** (`mimasc`) e **André Nóbrega** (`AndreNobrega125`).

| Frente | Responsável (revisão e explicação) |
|---|---|
| Domínio e exceções | Mirella |
| Persistência (JDBC/H2, DAOs) | Mirella |
| Previsões, alertas e testes | André |
| Menu, execução (Maven Wrapper) e README | André |
