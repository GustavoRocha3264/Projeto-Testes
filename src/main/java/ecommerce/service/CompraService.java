package ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import javax.transaction.Transactional;

@Service
public class CompraService
{
	private static final BigDecimal LIMITE_DESCONTO_10 = new BigDecimal("500.00");
	private static final BigDecimal LIMITE_DESCONTO_20 = new BigDecimal("1000.00");
	private static final BigDecimal DESCONTO_10_POR_VALOR = new BigDecimal("0.10");
	private static final BigDecimal DESCONTO_20_POR_VALOR = new BigDecimal("0.20");
	private static final BigDecimal FRETE_TAXA_FRAGIL = new BigDecimal("5.00");
	private static final BigDecimal FAIXA_A_LIMITE = new BigDecimal("5.00");
	private static final BigDecimal FAIXA_B_LIMITE = new BigDecimal("10.00");
	private static final BigDecimal FAIXA_C_LIMITE = new BigDecimal("50.00");
	private static final BigDecimal FAIXA_B_VALOR_KG = new BigDecimal("2.00");
	private static final BigDecimal FAIXA_C_VALOR_KG = new BigDecimal("4.00");
	private static final BigDecimal FAIXA_D_VALOR_KG = new BigDecimal("7.00");
	
	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal)
	{
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId)
	{
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel())
		{
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado())
		{
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso())
		{
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	} 

	/**
	 * Versão simplificada do cálculo de custo total.
	 * Regras:
	 * 1. Subtotal = soma do preço unitário * quantidade de cada item
	 * 2. Aplicar desconto por valor total:
	 *    - total >= 1000,00 → 20% desconto
	 *    - total >= 500,00 e < 1000,00 → 10% desconto
	 *    - demais → sem desconto
	 * 3. Calcular frete por peso físico total (somente peso físico):
	 *    - 0-5 kg → frete 0
	 *    - >5 e <=10 kg → R$ 2,00 por kg
	 *    - >10 e <=50 kg → R$ 4,00 por kg
	 *    - >50 kg → R$ 7,00 por kg
	 * 4. Produtos frágeis: adicionar R$ 5,00 POR UNIDADE
	 * 5. NÃO existe adicional por região nem desconto por fidelidade
	 * 6. Total = subtotalComDesconto + frete
	 * 7. Arredondamento final para 2 casas decimais (HALF_UP)
	 */
	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho)
	{
		if (carrinho == null || carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		
		// 1. Calcular subtotal
		BigDecimal subtotal = BigDecimal.ZERO;
		for (ItemCompra item : carrinho.getItens()) {
			BigDecimal precoUnitario = item.getProduto().getPreco();
			Long quantidade = item.getQuantidade();
			subtotal = subtotal.add(precoUnitario.multiply(new BigDecimal(quantidade)));
		}
		
		// 2. Aplicar desconto por valor total
		BigDecimal desconto = BigDecimal.ZERO;
		if (subtotal.compareTo(LIMITE_DESCONTO_20) >= 0) {
			desconto = subtotal.multiply(DESCONTO_20_POR_VALOR);
		} else if (subtotal.compareTo(LIMITE_DESCONTO_10) >= 0) {
			desconto = subtotal.multiply(DESCONTO_10_POR_VALOR);
		}
		
		BigDecimal subtotalComDesconto = subtotal.subtract(desconto);
		
		// 3. Calcular frete por peso físico total
		BigDecimal pesoTotal = BigDecimal.ZERO;
		for (ItemCompra item : carrinho.getItens()) {
			Produto produto = item.getProduto();
			Long quantidade = item.getQuantidade();
			BigDecimal pesoFisico = produto.getPesoFisico();
			pesoTotal = pesoTotal.add(pesoFisico.multiply(new BigDecimal(quantidade)));
		}
		
		BigDecimal frete = BigDecimal.ZERO;
		if (pesoTotal.compareTo(FAIXA_A_LIMITE) > 0 && pesoTotal.compareTo(FAIXA_B_LIMITE) <= 0) {
			frete = pesoTotal.multiply(FAIXA_B_VALOR_KG);
		} else if (pesoTotal.compareTo(FAIXA_B_LIMITE) > 0 && pesoTotal.compareTo(FAIXA_C_LIMITE) <= 0) {
			frete = pesoTotal.multiply(FAIXA_C_VALOR_KG);
		} else if (pesoTotal.compareTo(FAIXA_C_LIMITE) > 0) {
			frete = pesoTotal.multiply(FAIXA_D_VALOR_KG);
		}
		
		// 4. Adicionar taxa de produtos frágeis
		BigDecimal taxaFragil = BigDecimal.ZERO;
		for (ItemCompra item : carrinho.getItens()) {
			if (item.getProduto().isFragil()) {
				Long quantidade = item.getQuantidade();
				taxaFragil = taxaFragil.add(FRETE_TAXA_FRAGIL.multiply(new BigDecimal(quantidade)));
			}
		}
		
		frete = frete.add(taxaFragil);
		
		// 6. Total = subtotalComDesconto + frete
		BigDecimal total = subtotalComDesconto.add(frete);
		
		// 7. Arredondamento final para 2 casas decimais
		return total.setScale(2, RoundingMode.HALF_UP);
	}
}
