package ecommerce.service;

import static ecommerce.util.TestUtils.createCarrinho;
import static ecommerce.util.TestUtils.createItemCompra;
import static ecommerce.util.TestUtils.createProduto;
import static ecommerce.util.TestUtils.createProdutoComPesoCubico;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Testes de Caixa Preta - Particionamento de Equivalência.
 * 
 * Partições:
 * 1. Desconto por Tipo de Item (Qtde): < 3 (0%), 3-4 (5%), 5-7 (10%), >= 8 (15%)
 * 2. Desconto por Valor (Subtotal): <= 500 (0%), 500.01-1000 (10%), > 1000 (20%)
 * 3. Faixa de Frete (Peso Total): 0-5kg (Isento), 5.01-10kg (R$2/kg), 10.01-50kg (R$4/kg), > 50kg (R$7/kg)
 * 4. Desconto por Cliente (Nível): BRONZE (0%), PRATA (50%), OURO (100%)
 */
@DisplayName("Testes de Particionamento de Equivalência")
public class CompraServiceParticaoTest extends CompraServiceBaseTest {

    @Test
    @DisplayName("P1.1 - Sem Desconto por Tipo (Qtde < 3)")
    void calcularCustoTotal_semDescontoPorTipo_qtdeMenorQue3() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        
        var item1 = createItemCompra(produto, 2L);
        CarrinhoDeCompras carrinho = createCarrinho(Arrays.asList(item1));
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Qtde < 3").isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("P1.2 - Desconto de 5% por Tipo (Qtde 3-4)")
    void calcularCustoTotal_desconto5PorTipo_qtde3() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        
        var item1 = createItemCompra(produto, 3L);
        CarrinhoDeCompras carrinho = createCarrinho(Arrays.asList(item1));
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Qtde 3-4 (5% desc)").isEqualByComparingTo("285.00");
    }

    @Test
    @DisplayName("P1.3 - Desconto de 10% por Tipo (Qtde 5-7)")
    void calcularCustoTotal_desconto10PorTipo_qtde6() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ROUPA);
        
        var item1 = createItemCompra(produto, 6L);
        CarrinhoDeCompras carrinho = createCarrinho(Arrays.asList(item1));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Qtde 5-7 (10% desc)").isEqualByComparingTo("510.00");
    }

    @Test
    @DisplayName("P1.4 - Desconto de 15% por Tipo (Qtde >= 8)")
    void calcularCustoTotal_desconto15PorTipo_qtde8() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ALIMENTO);
        
        var item1 = createItemCompra(produto, 8L);
        CarrinhoDeCompras carrinho = createCarrinho(Arrays.asList(item1));
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Qtde >= 8 (15% desc)").isEqualByComparingTo("640.00");
    }

    @Test
    @DisplayName("P2.1 - Sem Desconto por Valor (Subtotal <= R$ 500.00)")
    void calcularCustoTotal_semDescontoPorValor_subtotal400() {
        var produto = createProduto(1L, new BigDecimal("400.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Subtotal <= 500").isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("P2.2 - Desconto de 10% por Valor (R$ 500.01 - R$ 1000.00)")
    void calcularCustoTotal_desconto10PorValor_subtotal750() {
        var produto = createProduto(1L, new BigDecimal("750.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Subtotal 500.01-1000 (10% desc)").isEqualByComparingTo("675.00");
    }

    @Test
    @DisplayName("P2.3 - Desconto de 20% por Valor (Subtotal > R$ 1000.00)")
    void calcularCustoTotal_desconto20PorValor_subtotal1500() {
        var produto = createProduto(1L, new BigDecimal("1500.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Subtotal > 1000 (20% desc)").isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("P3.1 - Frete Faixa A (0.01 - 5.00kg) - Isento")
    void calcularCustoTotal_freteFaixaA_isento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("3.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Frete Faixa A").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("P3.2 - Frete Faixa B (5.01 - 10.00kg) - R$2/kg + R$12")
    void calcularCustoTotal_freteFaixaB_7kg() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("7.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Frete Faixa B").isEqualByComparingTo("126.00");
    }

    @Test
    @DisplayName("P3.3 - Frete Faixa C (10.01 - 50.00kg) - R$4/kg + R$12")
    void calcularCustoTotal_freteFaixaC_20kg() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Frete Faixa C").isEqualByComparingTo("192.00");
    }

    @Test
    @DisplayName("P3.4 - Frete Faixa D (> 50.00kg) - R$7/kg + R$12")
    void calcularCustoTotal_freteFaixaD_60kg() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("60.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ROUPA);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Partição Frete Faixa D").isEqualByComparingTo("532.00");
    }

    @Test
    @DisplayName("P4.1 - Cliente BRONZE (0% desc. no frete)")
    void calcularCustoTotal_clienteBronze_freteIntegral() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("7.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ALIMENTO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Custo Total - Cliente BRONZE").isEqualByComparingTo("126.00");
    }

    @Test
    @DisplayName("P4.2 - Cliente PRATA (50% desc. no frete)")
    void calcularCustoTotal_clientePrata_frete50Porcento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("7.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.PRATA);

        assertThat(custoTotal).as("Custo Total - Cliente PRATA (50% desc)").isEqualByComparingTo("113.00");
    }

    @Test
    @DisplayName("P4.3 - Cliente OURO (100% desc. no frete)")
    void calcularCustoTotal_clienteOuro_freteGratis() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("7.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.MOVEL);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.OURO);

        assertThat(custoTotal).as("Custo Total - Cliente OURO (Frete Grátis)").isEqualByComparingTo("100.00");
    }
}

