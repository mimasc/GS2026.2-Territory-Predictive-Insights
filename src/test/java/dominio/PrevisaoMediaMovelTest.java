package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrevisaoMediaMovelTest {

    @Test
    void calculaMediaDasUltimasTresLeituras() throws Exception {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);
        List<Double> historico = List.of(1.0, 2.0, 3.0, 4.0, 8.0);

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: (3 + 4 + 8) / 3 = 5
        assertEquals(5.0, previsto, 0.0001);
    }

    @Test
    void historicoComPoucasLeiturasLancaDadosInvalidos() throws Exception {
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);

        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> estrategia.prever(List.of(1.0, 2.0, 3.0)));

        assertTrue(e.getMessage().contains("insuficiente"));
    }

    @Test
    void janelaForaDaFaixaLancaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel(0));
    }
}
