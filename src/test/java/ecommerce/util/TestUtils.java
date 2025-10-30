package ecommerce.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;

public class TestUtils {

    /**
     * Cria um objeto Produto com valores padronizados para dimensões e peso,
     * exceto pelos parâmetros fornecidos.
     */
    public static Produto createProduto(
            Long id,
            BigDecimal preco,
            BigDecimal pesoFisico,
            BigDecimal comprimento,
            BigDecimal largura,
            BigDecimal altura,
            Boolean fragil,
            TipoProduto tipo) {
        return new Produto(
                id,
                "Produto " + id,
                "Descricao",
                preco,
                pesoFisico,
                comprimento,
                largura,
                altura,
                fragil,
                tipo);
    }
    
    /**
     * Cria um objeto Produto com dimensões que resultam em um peso cúbico específico.
     */
    public static Produto createProdutoComPesoCubico(
            Long id,
            BigDecimal preco,
            BigDecimal pesoFisico,
            BigDecimal pesoCubico,
            Boolean fragil,
            TipoProduto tipo) {
        
        BigDecimal C = new BigDecimal("100.00");
        BigDecimal L = new BigDecimal("100.00");
        BigDecimal A = pesoCubico.multiply(new BigDecimal("6000"))
                                 .divide(C.multiply(L), 2, RoundingMode.HALF_UP);
        
        return new Produto(
                id,
                "Produto " + id,
                "Descricao",
                preco,
                pesoFisico,
                C,
                L,
                A,
                fragil,
                tipo);
    }

    /**
     * Cria um objeto ItemCompra.
     */
    public static ItemCompra createItemCompra(Produto produto, Long quantidade) {
        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        return item;
    }

    /**
     * Cria um objeto CarrinhoDeCompras a partir de uma lista de ItemCompra.
     */
    public static CarrinhoDeCompras createCarrinho(List<ItemCompra> itens) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        return carrinho;
    }
    
    /**
     * Cria um objeto CarrinhoDeCompras com um único item.
     */
    public static CarrinhoDeCompras createCarrinho(Produto produto, Long quantidade) {
        ItemCompra item = createItemCompra(produto, quantidade);
        return createCarrinho(Arrays.asList(item));
    }
}

