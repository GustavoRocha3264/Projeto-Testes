package ecommerce.dto;

import java.util.List;

public class DisponibilidadeDTO {
    private final Boolean disponivel;
    private final List<Long> idsProdutosIndisponiveis;

    public DisponibilidadeDTO(Boolean disponivel, List<Long> idsProdutosIndisponiveis) {
        this.disponivel = disponivel;
        this.idsProdutosIndisponiveis = idsProdutosIndisponiveis;
    }

    public Boolean disponivel() {
        return disponivel;
    }

    public List<Long> idsProdutosIndisponiveis() {
        return idsProdutosIndisponiveis;
    }
}

