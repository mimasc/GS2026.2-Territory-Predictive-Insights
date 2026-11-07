package excecao;

/** Lançada quando um dado viola as regras do domínio (faixa, população, limiar...). */
public class DadosInvalidosException extends Exception {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
