package ecommerce.fake;

import java.util.HashMap;
import java.util.Map;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;

/**
 * Implementação fake do CarrinhoDeComprasService para testes.
 * Simula persistência em memória.
 */
public class FakeCarrinhoDeComprasService {
    
    private Map<Long, CarrinhoDeCompras> carrinhos = new HashMap<>();
    
    public void salvar(CarrinhoDeCompras carrinho) {
        carrinhos.put(carrinho.getId(), carrinho);
    }
    
    public CarrinhoDeCompras buscarPorCarrinhoIdEClienteId(Long carrinhoId, Cliente cliente) {
        CarrinhoDeCompras carrinho = carrinhos.get(carrinhoId);
        if (carrinho != null && carrinho.getCliente().getId().equals(cliente.getId())) {
            return carrinho;
        }
        throw new IllegalArgumentException("Carrinho não encontrado para o cliente especificado.");
    }
    
    public CarrinhoDeCompras buscarPorId(Long id) {
        CarrinhoDeCompras carrinho = carrinhos.get(id);
        if (carrinho == null) {
            throw new IllegalArgumentException("Carrinho não encontrado.");
        }
        return carrinho;
    }
}
