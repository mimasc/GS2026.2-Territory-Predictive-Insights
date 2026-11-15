package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.PrevisaoMediaMovel;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ServicoMonitoramentoTest {
    private MedicaoDAO medicaoDAO;
    private AlertaDAO alertaDAO;
    private Sensor pluviometro;
    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() throws Exception {
        medicaoDAO = mock(MedicaoDAO.class);
        alertaDAO = mock(AlertaDAO.class);
        pluviometro = mock(Sensor.class);
        when(pluviometro.getId()).thenReturn("PLUV-01");
        when(pluviometro.getTipo()).thenReturn(TipoSensor.CHUVA);
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, new DadosTerritorio("Várzea", 500));
    }

    private List<Medicao> historicoDeChuva(double... valores) throws Exception {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao("PLUV-01", TipoSensor.CHUVA, v));
        }
        return lista;
    }

    @Test
    void previsaoAcimaDoLimiarGravaAlertaCritico() throws Exception {
        // Arrange: média das 3 últimas = 60 mm/h (limiar crítico = 50)
        when(medicaoDAO.buscarPorSensor(eq("PLUV-01"), anyInt())).thenReturn(historicoDeChuva(5, 10, 20, 55, 60, 65));

        // Act
        ResultadoPrevisao r = servico.avaliarSensor(pluviometro, new PrevisaoMediaMovel(3));

        // Assert
        assertEquals(NivelAlerta.CRITICO, r.nivel());
        assertTrue(r.temAlerta());
        assertTrue(r.interpretacao().contains("PLUV-01"));
        verify(alertaDAO).salvar(any(Alerta.class));
    }

    @Test
    void previsaoNormalNaoEmiteAlerta() throws Exception {
        when(medicaoDAO.buscarPorSensor(eq("PLUV-01"), anyInt())).thenReturn(historicoDeChuva(1, 2, 1, 2, 1, 2));

        ResultadoPrevisao r = servico.avaliarSensor(pluviometro, new PrevisaoMediaMovel(3));

        assertEquals(NivelAlerta.NENHUM, r.nivel());
        assertFalse(r.temAlerta());
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void sensorForaDeOperacaoNaoGravaMedicao() throws Exception {
        when(pluviometro.ler()).thenThrow(new FonteIndisponivelException("sensor desligado"));

        assertThrows(FonteIndisponivelException.class, () -> servico.registrarLeitura(pluviometro));

        verify(medicaoDAO, never()).salvar(any());
    }

    @Test
    void leituraValidaEhGravadaNoBanco() throws Exception {
        when(pluviometro.ler()).thenReturn(12.0);

        Medicao m = servico.registrarLeitura(pluviometro);

        assertEquals(12.0, m.getValor());
        verify(medicaoDAO).salvar(m);
    }

    @Test
    void alertaIdenticoSemLeituraNovaNaoEhGravadoDeNovo() throws Exception {
        // Arrange: previsão = 60 mm/h (CRÍTICO) e o último alerta do sensor é igual e posterior às leituras
        when(medicaoDAO.buscarPorSensor(eq("PLUV-01"), anyInt())).thenReturn(historicoDeChuva(5, 10, 20, 55, 60, 65));
        Alerta ultimo = new Alerta("PLUV-01", TipoSensor.CHUVA, NivelAlerta.CRITICO, 60.0, 50.0,
                "Média móvel", "alerta anterior", LocalDateTime.now().plusMinutes(1));
        when(alertaDAO.buscarUltimoPorSensor("PLUV-01")).thenReturn(Optional.of(ultimo));

        // Act
        ResultadoPrevisao r = servico.avaliarSensor(pluviometro, new PrevisaoMediaMovel(3));

        // Assert: o resultado ainda informa o alerta, mas ele não é gravado duas vezes
        assertEquals(NivelAlerta.CRITICO, r.nivel());
        assertTrue(r.interpretacao().contains("não foi gravado de novo"));
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void leituraNovaDepoisDoUltimoAlertaGravaAlertaDeNovo() throws Exception {
        // Arrange: o último alerta é anterior às leituras atuais, então há dado novo desde então
        when(medicaoDAO.buscarPorSensor(eq("PLUV-01"), anyInt())).thenReturn(historicoDeChuva(5, 10, 20, 55, 60, 65));
        Alerta antigo = new Alerta("PLUV-01", TipoSensor.CHUVA, NivelAlerta.CRITICO, 60.0, 50.0,
                "Média móvel", "alerta anterior", LocalDateTime.now().minusMinutes(5));
        when(alertaDAO.buscarUltimoPorSensor("PLUV-01")).thenReturn(Optional.of(antigo));

        // Act
        ResultadoPrevisao r = servico.avaliarSensor(pluviometro, new PrevisaoMediaMovel(3));

        // Assert
        assertFalse(r.interpretacao().contains("não foi gravado de novo"));
        verify(alertaDAO).salvar(any(Alerta.class));
    }
}
