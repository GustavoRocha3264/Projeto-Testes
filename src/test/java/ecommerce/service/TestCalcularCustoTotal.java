package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

/**
 * Testes para o método calcularCustoTotal() - versão simplificada.
 * Objetivo: 100% de cobertura de branch + 100% de mutantes mortos.
 */
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

    // ========== TESTES DE CARRINHO VAZIO/NULL ==========
    
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

    // ========== TESTES DE DESCONTO - FRONTEIRAS ==========
    
    @Test
    public void testDescontoSemDesconto_499_99() {
        // Total exatamente 499,99 - sem desconto
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("499.99"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 499.99, Desconto: 0, Peso: 1kg (frete 0), Total: 499.99
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("499.99"), resultado);
    }

    @Test
    public void testDescontoBorda_500_00_Exato() {
        // Total exatamente 500,00 - 10% desconto
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("500.00"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 500.00, Desconto: 50.00, SubtotalComDesconto: 450.00, Peso: 1kg (frete 0), Total: 450.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("450.00"), resultado);
    }

    @Test
    public void testDesconto10Porcento_999_99() {
        // Total exatamente 999,99 - 10% desconto
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("999.99"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 999.99, Desconto: 99.999, SubtotalComDesconto: 899.991 -> 899.99, Peso: 1kg (frete 0), Total: 899.99
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("899.99"), resultado);
    }

    @Test
    public void testDescontoBorda_1000_00_Exato() {
        // Total exatamente 1000,00 - 20% desconto
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1000.00"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 1000.00, Desconto: 200.00, SubtotalComDesconto: 800.00, Peso: 1kg (frete 0), Total: 800.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("800.00"), resultado);
    }

    @Test
    public void testDesconto20Porcento_Acima1000() {
        // Total acima de 1000,00 - 20% desconto
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1500.00"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 1500.00, Desconto: 300.00, SubtotalComDesconto: 1200.00, Peso: 1kg (frete 0), Total: 1200.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("1200.00"), resultado);
    }

    // ========== TESTES DE FRETE - FRONTEIRAS DE PESO ==========
    
    @Test
    public void testFretePeso_5kg_Exato() {
        // Peso exatamente 5kg - frete 0
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("5.0"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 5kg (frete 0), Total: 100.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFretePeso_5_01kg() {
        // Peso 5,01kg - R$ 2,00 por kg
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("5.01"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 5.01kg, Frete: 5.01 * 2.00 = 10.02, Total: 110.02
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("110.02"), resultado);
    }

    @Test
    public void testFretePeso_10kg_Exato() {
        // Peso exatamente 10kg - R$ 2,00 por kg
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.0"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 10kg, Frete: 10 * 2.00 = 20.00, Total: 120.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("120.00"), resultado);
    }

    @Test
    public void testFretePeso_10_01kg() {
        // Peso 10,01kg - R$ 4,00 por kg
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.01"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 10.01kg, Frete: 10.01 * 4.00 = 40.04, Total: 140.04
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("140.04"), resultado);
    }

    @Test
    public void testFretePeso_50kg_Exato() {
        // Peso exatamente 50kg - R$ 4,00 por kg
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.0"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 50kg, Frete: 50 * 4.00 = 200.00, Total: 300.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("300.00"), resultado);
    }

    @Test
    public void testFretePeso_50_01kg() {
        // Peso 50,01kg - R$ 7,00 por kg
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.01"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 50.01kg, Frete: 50.01 * 7.00 = 350.07, Total: 450.07
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("450.07"), resultado);
    }

    // ========== TESTES DE PRODUTOS FRÁGEIS ==========
    
    @Test
    public void testFragilZeroFrageis() {
        // Nenhum produto frágil
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 1kg (frete 0), Frágil: 0, Total: 100.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFragilUmItem() {
        // Um item frágil com quantidade 1
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), true)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 1kg (frete 0), Frágil: 1 * 5.00 = 5.00, Total: 105.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("105.00"), resultado);
    }

    @Test
    public void testFragilMultiplasUnidades() {
        // Um item frágil com quantidade 3 - deve cobrar R$5 por unidade
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("1.0"), true),
            3L
        );
        // Subtotal: 100 * 3 = 300.00, Desconto: 0, Peso: 3kg (frete 0), Frágil: 3 * 5.00 = 15.00, Total: 315.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("315.00"), resultado);
    }

    @Test
    public void testFragilVariosItens() {
        // Múltiplos itens frágeis com quantidades diferentes
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        
        // Item 1: 2 unidades frágeis
        Produto p1 = criarProduto(1L, "Produto1", new BigDecimal("50.00"), new BigDecimal("1.0"), true);
        ItemCompra item1 = new ItemCompra(1L, p1, 2L);
        itens.add(item1);
        
        // Item 2: 3 unidades frágeis
        Produto p2 = criarProduto(2L, "Produto2", new BigDecimal("30.00"), new BigDecimal("1.0"), true);
        ItemCompra item2 = new ItemCompra(2L, p2, 3L);
        itens.add(item2);
        
        carrinho.setItens(itens);
        
        // Subtotal: (50*2) + (30*3) = 100 + 90 = 190.00
        // Desconto: 0
        // Peso: 5kg (frete 0)
        // Frágil: (2 + 3) * 5.00 = 25.00
        // Total: 190.00 + 25.00 = 215.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("215.00"), resultado);
    }

    // ========== TESTES DE ARREDONDAMENTO ==========
    
    @Test
    public void testArredondamento() {
        // Caso que produz centavos com 3+ dígitos para confirmar arredondamento
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("33.333"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 33.333, Desconto: 0, Peso: 1kg (frete 0), Total: 33.33 (arredondado)
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("33.33"), resultado);
    }

    @Test
    public void testArredondamentoComDesconto() {
        // Desconto que gera arredondamento
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("1000.01"), new BigDecimal("1.0"), false)
        );
        // Subtotal: 1000.01, Desconto: 200.002 -> 200.00, SubtotalComDesconto: 800.01, Peso: 1kg (frete 0), Total: 800.01
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("800.01"), resultado);
    }

    // ========== TESTES COMBINADOS ==========
    
    @Test
    public void testCombinadoDescontoFreteFragil() {
        // Teste combinando desconto, frete e produtos frágeis
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("600.00"), new BigDecimal("6.0"), true),
            1L
        );
        // Subtotal: 600.00
        // Desconto: 10% = 60.00, SubtotalComDesconto: 540.00
        // Peso: 6kg, Frete: 6 * 2.00 = 12.00
        // Frágil: 1 * 5.00 = 5.00
        // Total: 540.00 + 12.00 + 5.00 = 557.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("557.00"), resultado);
    }

    @Test
    public void testCombinadoDesconto20FreteAltoFragilMultiplo() {
        // Teste combinando desconto 20%, frete alto e múltiplos frágeis
        CarrinhoDeCompras carrinho = criarCarrinhoComQuantidade(
            criarProduto(1L, "Produto1", new BigDecimal("1200.00"), new BigDecimal("60.0"), true),
            2L
        );
        // Subtotal: 1200 * 2 = 2400.00
        // Desconto: 20% = 480.00, SubtotalComDesconto: 1920.00
        // Peso: 120kg, Frete: 120 * 7.00 = 840.00
        // Frágil: 2 * 5.00 = 10.00
        // Total: 1920.00 + 840.00 + 10.00 = 2770.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("2770.00"), resultado);
    }

    @Test
    public void testMultiplosItensDiferentes() {
        // Múltiplos itens com características diferentes
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        List<ItemCompra> itens = new ArrayList<>();
        
        // Item 1: Produto normal
        Produto p1 = criarProduto(1L, "Produto1", new BigDecimal("250.00"), new BigDecimal("3.0"), false);
        ItemCompra item1 = new ItemCompra(1L, p1, 1L);
        itens.add(item1);
        
        // Item 2: Produto frágil
        Produto p2 = criarProduto(2L, "Produto2", new BigDecimal("300.00"), new BigDecimal("4.0"), true);
        ItemCompra item2 = new ItemCompra(2L, p2, 1L);
        itens.add(item2);
        
        carrinho.setItens(itens);
        
        // Subtotal: 250 + 300 = 550.00
        // Desconto: 10% = 55.00, SubtotalComDesconto: 495.00
        // Peso: 7kg, Frete: 7 * 2.00 = 14.00
        // Frágil: 1 * 5.00 = 5.00
        // Total: 495.00 + 14.00 + 5.00 = 514.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("514.00"), resultado);
    }

    // ========== TESTES PARA MATAR MUTANTES SOBREVIVENTES ==========
    
    @Test
    public void testCarrinhoComItensNull() {
        // Teste para matar mutante de verificação de itens null
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(null);
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    public void testFretePeso_Exatamente0kg() {
        // Peso exatamente 0kg - frete 0
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("0.0"), false)
        );
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void testFretePeso_10_00kg_Exato() {
        // Peso exatamente 10,00kg para testar boundary
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("10.00"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 10kg, Frete: 10 * 2.00 = 20.00, Total: 120.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("120.00"), resultado);
    }

    @Test
    public void testFretePeso_50_00kg_Exato() {
        // Peso exatamente 50,00kg para testar boundary
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarProduto(1L, "Produto1", new BigDecimal("100.00"), new BigDecimal("50.00"), false)
        );
        // Subtotal: 100.00, Desconto: 0, Peso: 50kg, Frete: 50 * 4.00 = 200.00, Total: 300.00
        BigDecimal resultado = compraService.calcularCustoTotal(carrinho);
        assertEquals(new BigDecimal("300.00"), resultado);
    }

    // ========== MÉTODOS AUXILIARES ==========
    
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
            new BigDecimal("10.0"), // comprimento
            new BigDecimal("10.0"), // largura
            new BigDecimal("10.0"), // altura
            fragil,
            TipoProduto.ELETRONICO
        );
    }
}
