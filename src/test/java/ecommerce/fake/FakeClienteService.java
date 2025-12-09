package ecommerce.fake;

import java.util.HashMap;
import java.util.Map;

import ecommerce.entity.Cliente;

/**
 * Implementação fake do ClienteService para testes.
 * Simula persistência em memória.
 */
public class FakeClienteService {
    
    private Map<Long, Cliente> clientes = new HashMap<>();
    
    public void salvar(Cliente cliente) {
        clientes.put(cliente.getId(), cliente);
    }
    
    public Cliente buscarPorId(Long id) {
        Cliente cliente = clientes.get(id);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }
        return cliente;
    }
}
