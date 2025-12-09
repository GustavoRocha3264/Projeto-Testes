package ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

public class TestCalcularCustoTotal {

    private CompraService compraService;

    @BeforeEach
    public void setUp() {
        CarrinhoDeComprasService carrinhoService = Mockito.mock(CarrinhoDeComprasService.class);
        ClienteService clienteService = Mockito.mock(ClienteService.class);
        IEstoqueExternal estoqueExternal = Mockito.mock(IEstoqueExternal.class);
        IPagamentoExternal pagamentoExternal = Mockito.mock(IPagamentoExternal.class);
        
        compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);
    }
    
    @Test
    public void testCarrinhoNull() {
        BigDecimal resultado = compraService.calcularCustoTotal(null);
        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    public void testCarrinhoVazio() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(new ArrayList<>());
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("0.00"), resultado);
    }
    
    @Test
    public void testDescontoSemDesconto_499_99() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("499.99"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("499.99"), resultado);
    }

    @Test
    public void testDescontoBorda_500_00_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("500.00"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("450.00"), resultado);
    }

    @Test
    public void testDesconto10Porcento_999_99() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("999.99"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("899.99"), resultado);
    }

    @Test
    public void testDescontoBorda_1000_00_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1000.00"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("800.00"), resultado);
    }

    @Test
    public void testDesconto20Porcento_Acima1000() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1500.00"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("1200.00"), resultado);
    }

    @Test
    public void testFretePeso_5kg_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("5.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFretePeso_5_01kg() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("5.01"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("110.02"), resultado);
    }

    @Test
    public void testFretePeso_10kg_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("120.00"), resultado);
    }

    @Test
    public void testFretePeso_10_01kg() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.01"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("140.04"), resultado);
    }

    @Test
    public void testFretePeso_50kg_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("300.00"), resultado);
    }

    @Test
    public void testFretePeso_50_01kg() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.01"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("450.07"), resultado);
    }
    
    @Test
    public void testFragilZeroFrageis() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFragilUmItem() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), true)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("105.00"), resultado);
    }

    @Test
    public void testFragilMultiplasUnidades() {
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), true),
            3L
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("315.00"), resultado);
    }

    @Test
    public void testFragilVariosItens() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        
        Produto p1 = criarProduto(1L, "Produto1", new BigDecimal("50.00"), new BigDecimal("1.0"), true);
        ItemCompra item1 = new ItemCompra(1L, p1, 2L);
        itens.add(item1);
        
        Produto p2 = criarProduto(2L, "Produto2", new BigDecimal("30.00"), new BigDecimal("1.0"), true);
        ItemCompra item2 = new ItemCompra(2L, p2, 3L);
        itens.add(item2);
        
        carrinho.setItens(itens);
        
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("215.00"), resultado);
    }

    @Test
    public void testArredondamento() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("33.333"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("33.33"), resultado);
    }

    @Test
    public void testArredondamentoComDesconto() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1000.01"), new BigDecimal("1.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("800.01"), resultado);
    }

    @Test
    public void testCombinadoDescontoFreteFragil() {
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("600.00"), new BigDecimal("6.0"), true),
            1L
        );

        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("557.00"), resultado);
    }

    @Test
    public void testCombinadoDesconto20FreteAltoFragilMultiplo() {
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("1200.00"), new BigDecimal("60.0"), true),
            2L
        );

        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("2770.00"), resultado);
    }

    @Test
    public void testMultiplosItensDiferentes() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        
        Produto p1 = criarProduto(1L, "Produto1", new BigDecimal("250.00"), new BigDecimal("3.0"), false);
        ItemCompra item1 = new ItemCompra(1L, p1, 1L);
        itens.add(item1);
        
        Produto p2 = criarProduto(2L, "Produto2", new BigDecimal("300.00"), new BigDecimal("4.0"), true);
        ItemCompra item2 = new ItemCompra(2L, p2, 1L);
        itens.add(item2);
        
        carrinho.setItens(itens);
        
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("514.00"), resultado);
    }

    @Test
    public void testCarrinhoComItensNull() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(null);
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    public void testFretePeso_Exatamente0kg() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("0.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFretePeso_10_00kg_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.00"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("120.00"), resultado);
    }

    @Test
    public void testFretePeso_50_00kg_Exato() {
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.00"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("300.00"), resultado);
    }
    
    private CarrinhoDeCompras criarCarrinho(Produto produto) {
        return criarCarrinhoComQuantidade(produto, 1L);
    }

    private CarrinhoDeCompras criarCarrinhoComQuantidade(Produto produto, Long quantidade) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        ItemCompra item = new ItemCompra(1L, produto, quantidade);
        itens.add(item);
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
