package ecommerce.dto;

public class EstoqueBaixaDTO {
    private final Boolean sucesso;

    public EstoqueBaixaDTO(Boolean sucesso) {
        this.sucesso = sucesso;
    }

    public Boolean sucesso() {
        return sucesso;
    }
}

