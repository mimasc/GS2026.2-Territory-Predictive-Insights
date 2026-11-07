package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DadosTerritorioTest {

    @Test
    void populacaoNegativaLancaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> new DadosTerritorio("Bairro", -1));
    }

    @Test
    void limiaresForaDeOrdemSaoRejeitadosEMantemOsAnteriores() throws Exception {
        DadosTerritorio dados = new DadosTerritorio("Bairro", 100);
        double atencaoAntes = dados.getLimiar(TipoSensor.CHUVA).getAtencao();

        assertThrows(DadosInvalidosException.class, () -> dados.setLimiar(TipoSensor.CHUVA, 40, 30, 50));

        assertEquals(atencaoAntes, dados.getLimiar(TipoSensor.CHUVA).getAtencao());
    }

    @Test
    void classificaValorNoMaiorNivelAtingido() throws Exception {
        DadosTerritorio dados = new DadosTerritorio("Bairro", 100); // chuva: 15 / 30 / 50

        assertEquals(NivelAlerta.NENHUM, dados.classificar(TipoSensor.CHUVA, 10));
        assertEquals(NivelAlerta.ATENCAO, dados.classificar(TipoSensor.CHUVA, 15));
        assertEquals(NivelAlerta.ALERTA, dados.classificar(TipoSensor.CHUVA, 35));
        assertEquals(NivelAlerta.CRITICO, dados.classificar(TipoSensor.CHUVA, 80));
    }
}
