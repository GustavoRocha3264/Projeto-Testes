package ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecommerce.dto.CompraDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import ecommerce.fake.FakeEstoqueExternal;
import ecommerce.fake.FakePagamentoExternal;

public class TestFinalizarCompraCenario1 {

    private CompraService compraService;
    private CarrinhoDeComprasService carrinhoService;
    private ClienteService clienteService;
    private FakeEstoqueExternal fakeEstoqueExternal;
    private FakePagamentoExternal fakePagamentoExternal;

    @BeforeEach
    public void setUp() {
        carrinhoService = Mockito.mock(CarrinhoDeComprasService.class);
        clienteService = Mockito.mock(ClienteService.class);
        
        fakeEstoqueExternal = new FakeEstoqueExternal();
        fakePagamentoExternal = new FakePagamentoExternal();
        
        compraService = new CompraService(carrinhoService, clienteService, 
                                         fakeEstoqueExternal, fakePagamentoExternal);
    }

    @Test
    public void testFinalizarCompraSucesso() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(99999L);
        fakeEstoqueExternal.setSucessoBaixa(true);
        
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        assertTrue(resultado.sucesso());
        assertEquals(99999L, resultado.transacaoPagamentoId());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    @Test
    public void testFinalizarCompraEstoqueIndisponivel() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        fakeEstoqueExternal.setDisponivel(false);
        List<Long> indisponiveis = new ArrayList<>();
        indisponiveis.add(1L);
        fakeEstoqueExternal.setProdutosIndisponiveis(indisponiveis);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Itens fora de estoque.", exception.getMessage());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    @Test
    public void testFinalizarCompraPagamentoNaoAutorizado() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(false); // Pagamento negado
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Pagamento não autorizado.", exception.getMessage());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    @Test
    public void testFinalizarCompraErroBaixaEstoque() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinho(carrinhoId, cliente);
        
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(88888L);
        fakeEstoqueExternal.setSucessoBaixa(false);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });
        
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        
        assertTrue(fakePagamentoExternal.isCancelamentoChamado());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

    @Test
    public void testFinalizarCompraMultiplosProdutos() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        Cliente cliente = criarCliente(clienteId, "Cliente Teste");
        CarrinhoDeCompras carrinho = criarCarrinhoMultiplosItens(carrinhoId, cliente);
        
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        fakeEstoqueExternal.setDisponivel(true);
        fakePagamentoExternal.setAutorizado(true);
        fakePagamentoExternal.setTransacaoId(77777L);
        fakeEstoqueExternal.setSucessoBaixa(true);
        
        CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
        
        assertTrue(resultado.sucesso());
        assertEquals(77777L, resultado.transacaoPagamentoId());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
    }

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
