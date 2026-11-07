package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicaoTest {

    @Test
    void criaMedicaoValidaComOsDadosInformados() throws Exception {
        // Arrange + Act
        Medicao m = new Medicao("PLUV-01", TipoSensor.CHUVA, 12.5);

        // Assert
        assertEquals("PLUV-01", m.getSensorId());
        assertEquals(12.5, m.getValor());
    }

    @Test
    void medicaoDeChuvaNegativaLancaDadosInvalidos() {
        // Arrange + Act
        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLUV-01", TipoSensor.CHUVA, -3.0));

        // Assert
        assertTrue(e.getMessage().contains("negativa"));
    }

    @Test
    void umidadeAcimaDeCemPorcentoLancaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> new Medicao("SOLO-01", TipoSensor.UMIDADE_SOLO, 101));
    }

    @Test
    void valorNaoNumericoLancaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> new Medicao("RIO-01", TipoSensor.NIVEL_RIO, Double.NaN));
    }
}
