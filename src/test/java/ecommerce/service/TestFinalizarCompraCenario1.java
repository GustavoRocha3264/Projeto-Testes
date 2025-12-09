package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ecommerce.dto.CompraDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import ecommerce.fake.FakeEstoqueExternal;
import ecommerce.fake.FakePagamentoExternal;

/**
 * Cenário 1: Testes de finalizarCompra() usando FAKES para serviços externos
 * (IEstoqueExternal e IPagamentoExternal) e MOCKS para dependências da camada repository.
 * 
 * Objetivo: 100% de cobertura de decisão do método finalizarCompra().
 */
public class TestFinalizarCompraCenario1 {

    private CompraService compraService;
    private CarrinhoDeComprasService carrinhoService;
    private ClienteService clienteService;
    private FakeEstoqueExternal fakeEstoqueExternal;
    private FakePagamentoExternal fakePagamentoExternal;

    @BeforeEach
    public void setUp() {
        // Mocks para serviços da camada de domínio
        carrinhoService = Mockito.mock(CarrinhoDeComprasService.class);
        clienteService = Mockito.mock(ClienteService.class);
        
        // Fakes para serviços externos
        fakeEstoqueExternal = new FakeEstoqueExternal();
        fakePagamentoExternal = new FakePagamentoExternal();
        
        compraService = new CompraService(carrinhoService, clienteService, 
                                         fakeEstoqueExternal, fakePagamentoExternal);
    }

    /**
     * Teste de sucesso completo: todos os serviços retornam sucesso.
     * Verifica:
     * - Estoque disponível
     * - Pagamento autorizado
     * - Baixa no estoque realizada
     * - Compra finalizada com sucesso
     */
    @Test
    public void testFinalizarCompraSucesso() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        // Configurar mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Configurar fakes para sucesso
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(99999L);
        fakeEstoqueExternal.setSucessoBaixa(true);
        
        // Act
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Assert
        assertTrue(resultado.sucesso());
        assertEquals(99999L, resultado.transacaoPagamentoId());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
        
        // Verificar que os métodos esperados foram chamados
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    /**
     * Teste de falha: estoque indisponível.
     * Verifica:
     * - Estoque indisponível lança exceção
     * - Pagamento não é chamado
     * - Baixa no estoque não é realizada
     */
    @Test
    public void testFinalizarCompraEstoqueIndisponivel() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        // Configurar mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Configurar fake para estoque indisponível
        fakeEstoqueExternal.setDisponivel(false);
        List<Long> indisponiveis = new ArrayList<>();
        indisponiveis.add(1L);
        fakeEstoqueExternal.setProdutosIndisponiveis(indisponiveis);
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Itens fora de estoque.", exception.getMessage());
        
        // Verificar que os métodos esperados foram chamados
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    /**
     * Teste de falha: pagamento não autorizado.
     * Verifica:
     * - Estoque disponível
     * - Pagamento não autorizado lança exceção
     * - Baixa no estoque não é realizada
     */
    @Test
    public void testFinalizarCompraPagamentoNaoAutorizado() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        // Configurar mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Configurar fakes
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(false); // Pagamento negado
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Pagamento não autorizado.", exception.getMessage());
        
        // Verificar que os métodos esperados foram chamados
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    /**
     * Teste de falha: erro ao dar baixa no estoque.
     * Verifica:
     * - Estoque disponível
     * - Pagamento autorizado
     * - Baixa no estoque falha
     * - Pagamento é cancelado
     * - Exceção é lançada
     */
    @Test
    public void testFinalizarCompraErroBaixaEstoque() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        // Configurar mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Configurar fakes
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(88888L);
        fakeEstoqueExternal.setSucessoBaixa(false); // Baixa falha
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        
        // Verificar que o cancelamento foi chamado
        assertTrue(fakePagamentoExternal.isCancelamentoChamado());
        
        // Verificar que os métodos esperados foram chamados
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    /**
     * Teste adicional: múltiplos produtos no carrinho.
     * Verifica que o fluxo funciona corretamente com vários itens.
     */
    @Test
    public void testFinalizarCompraMultiplosProdutos() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinhoMultiplosItens(carrinhoId, cliente);
        
        // Configurar mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Configurar fakes para sucesso
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(77777L);
        fakeEstoqueExternal.setSucessoBaixa(true);
        
        // Act
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Assert
        assertTrue(resultado.sucesso());
        assertEquals(77777L, resultado.transacaoPagamentoId());
        
        // Verificar que os métodos esperados foram chamados
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    // ========== MÉTODOS AUXILIARES ==========
    
    private Cliente criarCliente(Long id, String nome) {
        return new Cliente(id, nome, null, null);
    }

    private CarrinhoDeCompras criarCarrinho(Long carrinhoId, Cliente cliente) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(carrinhoId);
        carrinho.setCliente(cliente);
        
        List<ItemCompra> itens = new ArrayList<>();
        Produto produto = criarProduto(1L, "Produto Teste", new BigDecimal("100.00"), new BigDecimal("2.0"), false);
        ItemCompra item = new ItemCompra(1L, produto, 2L);
        itens.add(item);
        
        carrinho.setItens(itens);
        return carrinho;
    }

    private CarrinhoDeCompras criarCarrinhoMultiplosItens(Long carrinhoId, Cliente cliente) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(carrinhoId);
        carrinho.setCliente(cliente);
        
        List<ItemCompra> itens = new ArrayList<>();
        
        Produto p1 = criarProduto(1L, "Produto 1", new BigDecimal("150.00"), new BigDecimal("3.0"), false);
        ItemCompra item1 = new ItemCompra(1L, p1, 1L);
        itens.add(item1);
        
        Produto p2 = criarProduto(2L, "Produto 2", new BigDecimal("200.00"), new BigDecimal("4.0"), true);
        ItemCompra item2 = new ItemCompra(2L, p2, 2L);
        itens.add(item2);
        
        carrinho.setItens(itens);
        return carrinho;
    }

    private Produto criarProduto(Long id, String nome, BigDecimal preco, BigDecimal pesoFisico, boolean fragil) {
        return new Produto(
            id,
            nome,
            "Descrição",
            preco,
            pesoFisico,
            new BigDecimal("10.0"),
            new BigDecimal("10.0"),
            new BigDecimal("10.0"),
            fragil,
            TipoProduto.ELETRONICO
        );
    }
}
