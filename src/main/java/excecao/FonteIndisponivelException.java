package excecao;

/** Lançada quando uma fonte de dados (sensor, gerador ou banco) não consegue responder. */
public class FonteIndisponivelException extends Exception {
    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
