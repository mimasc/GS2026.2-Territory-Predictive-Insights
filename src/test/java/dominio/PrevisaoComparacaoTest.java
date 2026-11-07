package dominio;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrevisaoComparacaoTest {

    @Test
    void projetaTendenciaDeAltaAlemDaMediaMovel() throws Exception {
        // Arrange: período anterior (10,10,10) e recente (20,20,20)
        List<Double> historico = List.of(10.0, 10.0, 10.0, 20.0, 20.0, 20.0);
        PrevisaoComparacao comparacao = new PrevisaoComparacao(3);
        PrevisaoMediaMovel media = new PrevisaoMediaMovel(3);

        // Act
        double porComparacao = comparacao.prever(historico);
        double porMedia = media.prever(historico);

        // Assert: comparação projeta 20 + (20 - 10) = 30; média móvel fica em 20
        assertEquals(30.0, porComparacao, 0.0001);
        assertEquals(20.0, porMedia, 0.0001);
    }

    @Test
    void previsaoNuncaFicaNegativaQuandoOsValoresCaem() throws Exception {
        List<Double> historico = List.of(20.0, 20.0, 20.0, 1.0, 1.0, 1.0);

        double previsto = new PrevisaoComparacao(3).prever(historico);

        assertEquals(0.0, previsto, 0.0001);
    }
}
