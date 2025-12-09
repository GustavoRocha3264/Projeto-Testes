package ecommerce.fake;

import java.util.ArrayList;
import java.util.List;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

public class FakeEstoqueExternal implements IEstoqueExternal {
    
    private boolean disponivel = true;
    private boolean sucessoBaixa = true;
    private List<Long> produtosIndisponiveis = new ArrayList<>();
    
    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
    
    public void setSucessoBaixa(boolean sucessoBaixa) {
        this.sucessoBaixa = sucessoBaixa;
    }
    
    public void setProdutosIndisponiveis(List<Long> produtosIndisponiveis) {
        this.produtosIndisponiveis = produtosIndisponiveis;
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
        return new DisponibilidadeDTO(disponivel, produtosIndisponiveis);
    }

    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
        return new EstoqueBaixaDTO(sucessoBaixa);
    }
}
