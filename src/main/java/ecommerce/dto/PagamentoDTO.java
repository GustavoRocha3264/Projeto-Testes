package ecommerce.dto;

public class PagamentoDTO {
    private final Boolean autorizado;
    private final Long transacaoId;

    public PagamentoDTO(Boolean autorizado, Long transacaoId) {
        this.autorizado = autorizado;
        this.transacaoId = transacaoId;
    }

    public Boolean autorizado() {
        return autorizado;
    }

    public Long transacaoId() {
        return transacaoId;
    }
}

