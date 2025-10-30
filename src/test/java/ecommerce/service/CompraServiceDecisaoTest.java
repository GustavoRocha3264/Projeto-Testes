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
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

/**
 * Testes de Caixa Preta - Tabela de Decisão.
 * 
 * Regra de Decisão Composta: Frete (Peso + Fragilidade + Região + Nível Cliente)
 * 
 * Regra 1: Frete Isento (Peso <= 5kg) - Cliente Bronze - Sudeste - Não Frágil
 * Regra 2: Frete Faixa D (Peso > 50kg) - Cliente Prata - Norte - Frágil
 * Regra 3: Frete Faixa B (5kg < Peso <= 10kg) - Cliente Ouro - Nordeste - Não Frágil
 * Regra 4: Frete Faixa C (10kg < Peso <= 50kg) - Cliente Bronze - Sul - Frágil
 */
@DisplayName("Testes de Tabela de Decisão")
public class CompraServiceDecisaoTest extends CompraServiceBaseTest {

    @Test
    @DisplayName("R1 - Frete Isento (Peso 4kg) + Cliente BRONZE + SUDESTE")
    void calcularCustoTotal_regra1_freteIsento() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("4.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Regra 1: Frete Isento").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("R2 - Frete Faixa D (Peso 60kg) + Cliente PRATA + NORTE + Frágil")
    void calcularCustoTotal_regra2_freteFaixaD_fragil_prata_norte() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("60.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), true, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.NORTE, TipoCliente.PRATA);

        assertThat(custoTotal).as("Regra 2: Faixa D, Frágil, Prata, Norte").isEqualByComparingTo("384.05");
    }

    @Test
    @DisplayName("R3 - Frete Faixa B (Peso 8kg) + Cliente OURO + NORDESTE")
    void calcularCustoTotal_regra3_freteFaixaB_ouro_nordeste() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("8.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO);

        assertThat(custoTotal).as("Regra 3: Faixa B, Ouro, Nordeste").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("R4 - Frete Faixa C (Peso 20kg) + Cliente BRONZE + SUL + Frágil")
    void calcularCustoTotal_regra4_freteFaixaC_fragil_bronze_sul() {
        var produto = createProduto(1L, new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), true, TipoProduto.LIVRO);
        CarrinhoDeCompras carrinho = createCarrinho(produto, 1L);
        

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUL, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Regra 4: Faixa C, Frágil, Bronze, Sul").isEqualByComparingTo("201.85");
    }

    @Test
    @DisplayName("R5 - Acumulação de Descontos: Tipo (15%) + Valor (>1000, 20%)")
    void calcularCustoTotal_regra5_acumulacaoDescontos() {
        var produto = createProduto(1L, new BigDecimal("150.00"), new BigDecimal("1.00"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), false, TipoProduto.ELETRONICO);
        
        CarrinhoDeCompras carrinho = createCarrinho(produto, 8L);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);

        assertThat(custoTotal).as("Regra 5: Acumulação de Descontos").isEqualByComparingTo("844.00");
    }
}

