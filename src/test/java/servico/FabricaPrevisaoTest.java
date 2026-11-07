package servico;

import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaPrevisaoTest {
    private final FabricaPrevisao fabrica = new FabricaPrevisao();

    @Test
    void criaMediaMovelAPartirDaChave() throws Exception {
        assertInstanceOf(PrevisaoMediaMovel.class, fabrica.criar("media", 3));
    }

    @Test
    void criaComparacaoAPartirDaChave() throws Exception {
        assertInstanceOf(PrevisaoComparacao.class, fabrica.criar("COMPARACAO", 3));
    }

    @Test
    void chaveDesconhecidaLancaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar("astrologia", 3));
    }

    @Test
    void configuracaoEhSingleton() {
        assertSame(ConfiguracaoSistema.getInstance(), ConfiguracaoSistema.getInstance());
    }
}
