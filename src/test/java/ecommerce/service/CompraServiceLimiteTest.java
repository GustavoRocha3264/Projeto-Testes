package ecommerce.service;

import static ecommerce.util.TestUtils.createCarrinho;
import static ecommerce.util.TestUtils.createItemCompra;
import static ecommerce.util.TestUtils.createProduto;
import static ecommerce.util.TestUtils.createProdutoComPesoCubico;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Testes de Caixa Preta - Análise de Valores Limites.
 * 
 * Limites:
 * 1. Desconto por Tipo de Item (Qtde): 2, 3 | 4, 5 | 7, 8
 * 2. Desconto por Valor (Subtotal): 500.00, 500.01 | 1000.00, 1000.01
 * 3. Faixa de Frete (Peso Total): 5.00, 5.01 | 10.00, 10.01 | 50.00, 50.01
 */
@DisplayName("Testes de Análise de Valores Limites")
public class CompraServiceLimiteTest extends CompraServiceBaseTest {

    @Test
    @DisplayName("L1.1 - Qtde = 2 (Limite Superior da Partição 0%)")
    void calcularCustoTotal_limiteQtde2_semDesconto() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 2L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 2").isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("L1.2 - Qtde = 3 (Limite Inferior da Partição 5%)")
    void calcularCustoTotal_limiteQtde3_desconto5Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 3L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 3").isEqualByComparingTo("285.00");
    }

    @Test
    @DisplayName("L1.3 - Qtde = 4 (Limite Superior da Partição 5%)")
    void calcularCustoTotal_limiteQtde4_desconto5Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 4L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 4").isEqualByComparingTo("380.00");
    }

    @Test
    @DisplayName("L1.4 - Qtde = 5 (Limite Inferior da Partição 10%)")
    void calcularCustoTotal_limiteQtde5_desconto10Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 5L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 5").isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("L1.5 - Qtde = 7 (Limite Superior da Partição 10%)")
    void calcularCustoTotal_limiteQtde7_desconto10Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 7L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 7").isEqualByComparingTo("593.00");
    }

    @Test
    @DisplayName("L1.6 - Qtde = 8 (Limite Inferior da Partição 15%)")
    void calcularCustoTotal_limiteQtde8_desconto15Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 8L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Qtde 8").isEqualByComparingTo("640.00");
    }

    @Test
    @DisplayName("L2.1 - Subtotal = R$ 500.00 (Limite Superior da Partição 0%)")
    void calcularCustoTotal_limiteSubtotal500_semDesconto() {
        var produto = createProduto(1L, new BigDecimal("500.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Subtotal 500.00").isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("L2.2 - Subtotal = R$ 500.01 (Limite Inferior da Partição 10%)")
    void calcularCustoTotal_limiteSubtotal500_01_desconto10Porcento() {
        var produto = createProduto(1L, new BigDecimal("500.01"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Subtotal 500.01").isEqualByComparingTo("450.01");
    }

    @Test
    @DisplayName("L2.3 - Subtotal = R$ 1000.00 (Limite Superior da Partição 10%)")
    void calcularCustoTotal_limiteSubtotal1000_desconto10Porcento() {
        var produto = createProduto(1L, new BigDecimal("1000.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Subtotal 1000.00").isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("L2.4 - Subtotal = R$ 1000.01 (Limite Inferior da Partição 20%)")
    void calcularCustoTotal_limiteSubtotal1000_01_desconto20Porcento() {
        var produto = createProduto(1L, new BigDecimal("1000.01"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Subtotal 1000.01").isEqualByComparingTo("800.01");
    }

    @Test
    @DisplayName("L3.1 - Peso Total = 5.00kg (Limite Superior Faixa A - Isento)")
    void calcularCustoTotal_limitePeso5_00_isento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 5.00kg").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("L3.2 - Peso Total = 5.01kg (Limite Inferior Faixa B - R$2/kg + R$12)")
    void calcularCustoTotal_limitePeso5_01_faixaB() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("5.01"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 5.01kg").isEqualByComparingTo("122.02");
    }

    @Test
    @DisplayName("L3.3 - Peso Total = 10.00kg (Limite Superior Faixa B - R$2/kg + R$12)")
    void calcularCustoTotal_limitePeso10_00_faixaB() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 10.00kg").isEqualByComparingTo("132.00");
    }

    @Test
    @DisplayName("L3.4 - Peso Total = 10.01kg (Limite Inferior Faixa C - R$4/kg + R$12)")
    void calcularCustoTotal_limitePeso10_01_faixaC() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("10.01"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 10.01kg").isEqualByComparingTo("152.04");
    }

    @Test
    @DisplayName("L3.5 - Peso Total = 50.00kg (Limite Superior Faixa C - R$4/kg + R$12)")
    void calcularCustoTotal_limitePeso50_00_faixaC() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("50.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 50.00kg").isEqualByComparingTo("312.00");
    }

    @Test
    @DisplayName("L3.6 - Peso Total = 50.01kg (Limite Inferior Faixa D - R$7/kg + R$12)")
    void calcularCustoTotal_limitePeso50_01_faixaD() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("50.01"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(custoTotal).as("Peso 50.01kg").isEqualByComparingTo("462.07");
    }
}

