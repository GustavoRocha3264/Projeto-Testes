package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
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
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.fake.FakeCarrinhoDeComprasService;
import ecommerce.fake.FakeClienteService;

/**
 * Cenário 2: Testes de finalizarCompra() usando MOCKS para serviços externos
 * (IEstoqueExternal e IPagamentoExternal) e FAKES para dependências da camada repository.
 * 
 * Objetivo: 100% de cobertura de decisão do método finalizarCompra().
 */
public class TestFinalizarCompraCenario2 {

    private CompraService compraService;
    private FakeCarrinhoDeComprasService fakeCarrinhoService;
    private FakeClienteService fakeClienteService;
    private IEstoqueExternal mockEstoqueExternal;
    private IPagamentoExternal mockPagamentoExternal;

    @BeforeEach
    public void setUp() {
        // Fakes para serviços da camada de domínio (simulam persistência)
        fakeCarrinhoService = new FakeCarrinhoDeComprasService();
        fakeClienteService = new FakeClienteService();
        
        // Mocks para serviços externos
        mockEstoqueExternal = Mockito.mock(IEstoqueExternal.class);
        mockPagamentoExternal = Mockito.mock(IPagamentoExternal.class);
        
        // Criar CompraService com adapter para os fakes
        CarrinhoDeComprasService carrinhoServiceAdapter = new CarrinhoDeComprasService(null) {
            @Override
            public CarrinhoDeCompras buscarPorCarrinhoIdEClienteId(Long carrinhoId, Cliente cliente) {
                return fakeCarrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
            }
        };
        
        ClienteService clienteServiceAdapter = new ClienteService(null) {
            @Override
            public Cliente buscarPorId(Long id) {
                return fakeClienteService.buscarPorId(id);
            }
        };
        
        compraService = new CompraService(carrinhoServiceAdapter, clienteServiceAdapter, 
                                         mockEstoqueExternal, mockPagamentoExternal);
    }

    /**
     * Teste de sucesso completo: todos os serviços retornam sucesso.
     * Verifica:
     * - Estoque disponível (mock)
     * - Pagamento autorizado (mock)
     * - Baixa no estoque realizada (mock)
     * - Compra finalizada com sucesso
     */
    @Test
    public void testFinalizarCompraSucesso() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mocks para sucesso
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
        when(mockPagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(true, 11111L));
        when(mockEstoqueExternal.darBaixa(anyList(), anyList()))
            .thenReturn(new EstoqueBaixaDTO(true));
        
        // Act
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Assert
        assertTrue(resultado.sucesso());
        assertEquals(11111L, resultado.transacaoPagamentoId());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
        
        // Verificar que os métodos esperados foram chamados
        verify(mockEstoqueExternal).verificarDisponibilidade(anyList(), anyList());
        verify(mockPagamentoExternal).autorizarPagamento(anyLong(), anyDouble());
        verify(mockEstoqueExternal).darBaixa(anyList(), anyList());
        verify(mockPagamentoExternal, never()).cancelarPagamento(anyLong(), anyLong());
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
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mock para estoque indisponível
        List<Long> indisponiveis = new ArrayList<>();
        indisponiveis.add(1L);
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(false, indisponiveis));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Itens fora de estoque.", exception.getMessage());
        
        // Verificar que pagamento e baixa não foram chamados
        verify(mockEstoqueExternal).verificarDisponibilidade(anyList(), anyList());
        verify(mockPagamentoExternal, never()).autorizarPagamento(anyLong(), anyDouble());
        verify(mockEstoqueExternal, never()).darBaixa(anyList(), anyList());
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
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mocks
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
        when(mockPagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(false, null)); // Pagamento negado
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Pagamento não autorizado.", exception.getMessage());
        
        // Verificar que baixa não foi chamada
        verify(mockEstoqueExternal).verificarDisponibilidade(anyList(), anyList());
        verify(mockPagamentoExternal).autorizarPagamento(anyLong(), anyDouble());
        verify(mockEstoqueExternal, never()).darBaixa(anyList(), anyList());
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
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mocks
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
        when(mockPagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(true, 22222L));
        when(mockEstoqueExternal.darBaixa(anyList(), anyList()))
            .thenReturn(new EstoqueBaixaDTO(false)); // Baixa falha
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        
        // Verificar que o cancelamento foi chamado
        verify(mockEstoqueExternal).verificarDisponibilidade(anyList(), anyList());
        verify(mockPagamentoExternal).autorizarPagamento(anyLong(), anyDouble());
        verify(mockEstoqueExternal).darBaixa(anyList(), anyList());
        verify(mockPagamentoExternal).cancelarPagamento(clienteId, 22222L);
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
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mocks para sucesso
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
        when(mockPagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(true, 33333L));
        when(mockEstoqueExternal.darBaixa(anyList(), anyList()))
            .thenReturn(new EstoqueBaixaDTO(true));
        
        // Act
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Assert
        assertTrue(resultado.sucesso());
        assertEquals(33333L, resultado.transacaoPagamentoId());
        
        // Verificar que os métodos esperados foram chamados
        verify(mockEstoqueExternal).verificarDisponibilidade(anyList(), anyList());
        verify(mockPagamentoExternal).autorizarPagamento(anyLong(), anyDouble());
        verify(mockEstoqueExternal).darBaixa(anyList(), anyList());
    }

    /**
     * Teste de cobertura adicional: verifica comportamento com carrinho vazio.
     * Nota: Este teste pode falhar se a implementação não tratar carrinhos vazios,
     * mas é importante para cobertura completa.
     */
    @Test
    public void testFinalizarCompraCarrinhoVazio() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(carrinhoId);
        carrinho.setCliente(cliente);
        carrinho.setItens(new ArrayList<>()); // Carrinho vazio
        
        // Salvar nos fakes
        fakeClienteService.salvar(cliente);
        fakeCarrinhoService.salvar(carrinho);
        
        // Configurar mocks
        when(mockEstoqueExternal.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
        when(mockPagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(true, 44444L));
        when(mockEstoqueExternal.darBaixa(anyList(), anyList()))
            .thenReturn(new EstoqueBaixaDTO(true));
        
        // Act
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Assert
        assertTrue(resultado.sucesso());
        assertEquals(44444L, resultado.transacaoPagamentoId());
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
        
        Produto p3 = criarProduto(3L, "Produto 3", new BigDecimal("50.00"), new BigDecimal("1.0"), false);
        ItemCompra item3 = new ItemCompra(3L, p3, 3L);
        itens.add(item3);
        
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
