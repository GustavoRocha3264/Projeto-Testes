package ecommerce.service;

import static ecommerce.util.TestUtils.createCarrinho;
import static ecommerce.util.TestUtils.createItemCompra;
import static ecommerce.util.TestUtils.createProduto;
import static ecommerce.util.TestUtils.createProdutoComPesoCubico;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Testes de Caixa Branca (Cobertura de Arestas) e Robustez (assertThrows).
 */
@DisplayName("Testes de Cobertura e Robustez")
public class CompraServiceWhiteBoxTest extends CompraServiceBaseTest {

    private final Produto PROD_BASE = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
    
    private final Produto PROD_FRAGIL = createProduto(2L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), true, TipoProduto.MOVEL);

    @Test
    @DisplayName("R1 - Carrinho Nulo deve retornar 0.00")
    void calcularCustoTotal_carrinhoNulo_retornaZero() {
        BigDecimal custoTotal = compraService.calcularCustoTotal(null, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Carrinho nulo").isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("R2 - Carrinho com lista de itens nula deve retornar 0.00")
    void calcularCustoTotal_listaItensNula_retornaZero() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(null);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Lista de itens nula").isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("R3 - Carrinho com lista de itens vazia deve retornar 0.00")
    void calcularCustoTotal_listaItensVazia_retornaZero() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.emptyList());
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Lista de itens vazia").isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("R4 - Quantidade negativa deve lançar exceção")
    void calcularCustoTotal_quantidadeNegativa_lancaExcecao() {
        Produto produto = createProduto(1L, new BigDecimal("10.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        ItemCompra item = createItemCompra(produto, -1L);
        CarrinhoDeCompras carrinho = createCarrinho(Arrays.asList(item));
        
        assertThrows(IllegalArgumentException.class, () ->
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE),
            "Esperado ArithmeticException para quantidade negativa"
        );
    }
    
    @Test
    @DisplayName("R5 - Preço negativo deve lançar exceção")
    void calcularCustoTotal_precoNegativo_lancaExcecao() {
        Produto produto = createProduto(1L, new BigDecimal("-10.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        assertThrows(IllegalArgumentException.class, () ->
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE),
            "Esperado exceção para preço negativo"
        );
    }

    private static Stream<Arguments> freteCenarios() {
        return Stream.of(
            Arguments.of("Faixa A (Isento)", 4.00, 100.00, "100.00"),
            Arguments.of("Faixa B (Limite Inferior)", 5.01, 100.00, "122.02"),
            Arguments.of("Faixa C (Meio)", 25.00, 100.00, "212.00"),
            Arguments.of("Faixa D (Acima)", 55.00, 100.00, "497.00")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("freteCenarios")
    @DisplayName("BC1 - Cobertura de todas as faixas de frete")
    void calcularCustoTotal_coberturaFaixasFrete(String nomeCenario, double peso, double preco, String totalEsperado) {
        Produto produto = createProduto(1L, new BigDecimal(String.valueOf(preco)), new BigDecimal(String.valueOf(peso)), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        
        assertThat(custoTotal).as(nomeCenario).isEqualByComparingTo(totalEsperado);
    }

    @Test
    @DisplayName("BC2 - Cobertura de frete com Taxa Frágil")
    void calcularCustoTotal_coberturaTaxaFragil() {
        CarrinhoDeCompras carrinho = createCarrinho(PROD_FRAGIL, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        
        assertThat(custoTotal).as("Taxa Frágil aplicada").isEqualByComparingTo("105.00");
    }
    
    @Test
    @DisplayName("BC3 - Cobertura de Frete com Peso Cúbico > Peso Físico")
    void calcularCustoTotal_coberturaPesoCubicoMaior() {
        Produto produto = createProdutoComPesoCubico(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("5.00"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        
        assertThat(custoTotal).as("Peso Tributável = Peso Cúbico").isEqualByComparingTo("100.00");
    }
    
    @Test
    @DisplayName("MC/DC-1: Peso 7.00kg (C1=T, C2=T) Faixa B (Frete 26.00)")
    void calcularCustoTotal_mcdc1_peso7kg() {
        Produto produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("7.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("MC/DC 1 (T, T)").isEqualByComparingTo("126.00");
    }

    @Test
    @DisplayName("MC/DC-2: Peso 5.00kg (C1=F, C2=T) Faixa A (Frete 0.00)")
    void calcularCustoTotal_mcdc2_peso5kg() {
        Produto produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("MC/DC 2 (F, T)").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("MC/DC-3: Peso 10.01kg (C1=T, C2=F) Faixa C (Frete 52.04)")
    void calcularCustoTotal_mcdc3_peso10_01kg() {
        Produto produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("10.01"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("MC/DC 3 (T, F)").isEqualByComparingTo("152.04");
    }
}

