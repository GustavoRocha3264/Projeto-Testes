package ecommerce.fake;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

/**
 * Implementação fake do serviço de pagamento externo para testes.
 * Permite configurar o comportamento esperado.
 */
public class FakePagamentoExternal implements IPagamentoExternal {
    
    private boolean autorizado = true;
    private Long transacaoId = 12345L;
    private boolean cancelamentoChamado = false;
    
    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }
    
    public void setTransacaoId(Long transacaoId) {
        this.transacaoId = transacaoId;
    }
    
    public boolean isCancelamentoChamado() {
        return cancelamentoChamado;
    }

    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        return new PagamentoDTO(autorizado, transacaoId);
    }

    @Override
    public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId) {
        this.cancelamentoChamado = true;
    }
}
