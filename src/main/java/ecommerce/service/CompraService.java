package ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
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
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import javax.transaction.Transactional;

@Service
public class CompraService
{
	private static final BigDecimal FATOR_PESO_CUBICO = new BigDecimal("6000");
	private static final BigDecimal DESCONTO_5_POR_TIPO = new BigDecimal("0.05");
	private static final BigDecimal DESCONTO_10_POR_TIPO = new BigDecimal("0.10");
	private static final BigDecimal DESCONTO_15_POR_TIPO = new BigDecimal("0.15");
	private static final BigDecimal DESCONTO_10_POR_VALOR = new BigDecimal("0.10");
	private static final BigDecimal DESCONTO_20_POR_VALOR = new BigDecimal("0.20");
	private static final BigDecimal LIMITE_DESCONTO_10 = new BigDecimal("500.00");
	private static final BigDecimal LIMITE_DESCONTO_20 = new BigDecimal("1000.00");
	private static final BigDecimal FRETE_TAXA_MINIMA = new BigDecimal("12.00");
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

		BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());

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

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente)
	{
		if (carrinho == null || carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		
			BigDecimal subtotal = carrinho.getItens().stream()
					.map(item -> {
						if (item.getQuantidade() <= 0) {
							throw new IllegalArgumentException("Quantidade do item não pode ser zero ou negativa.");
						}
						if (item.getProduto().getPreco().compareTo(BigDecimal.ZERO) < 0) {
							throw new IllegalArgumentException("Preço do produto não pode ser negativo.");
						}
						return item.getProduto().getPreco().multiply(new BigDecimal(item.getQuantidade()));
					})
					.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal subtotalComDesconto = subtotal;
		
		Map<TipoProduto, Long> contagemPorTipo = carrinho.getItens().stream()
				.collect(Collectors.groupingBy(item -> item.getProduto().getTipo(),
						Collectors.summingLong(ItemCompra::getQuantidade)));
		
		BigDecimal descontoPorTipo = BigDecimal.ZERO;
		
		for (Map.Entry<TipoProduto, Long> entry : contagemPorTipo.entrySet()) {
			TipoProduto tipo = entry.getKey();
			Long totalItensDoTipo = entry.getValue();
			
			BigDecimal percentualDesconto = BigDecimal.ZERO;
			if (totalItensDoTipo >= 3 && totalItensDoTipo <= 4) {
				percentualDesconto = DESCONTO_5_POR_TIPO;
			} else if (totalItensDoTipo >= 5 && totalItensDoTipo <= 7) {
				percentualDesconto = DESCONTO_10_POR_TIPO;
			} else if (totalItensDoTipo >= 8) {
				percentualDesconto = DESCONTO_15_POR_TIPO;
			}
			
			if (percentualDesconto.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal subtotalDoTipo = carrinho.getItens().stream()
						.filter(item -> item.getProduto().getTipo() == tipo)
						.map(item -> item.getProduto().getPreco().multiply(new BigDecimal(item.getQuantidade())))
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				
				descontoPorTipo = descontoPorTipo.add(subtotalDoTipo.multiply(percentualDesconto));
			}
		}
		
		subtotalComDesconto = subtotalComDesconto.subtract(descontoPorTipo);
		
		BigDecimal descontoPorValor = BigDecimal.ZERO;
		if (subtotalComDesconto.compareTo(LIMITE_DESCONTO_20) > 0) {
			descontoPorValor = subtotalComDesconto.multiply(DESCONTO_20_POR_VALOR);
		} else if (subtotalComDesconto.compareTo(LIMITE_DESCONTO_10) > 0) {
			descontoPorValor = subtotalComDesconto.multiply(DESCONTO_10_POR_VALOR);
		}
		
		subtotalComDesconto = subtotalComDesconto.subtract(descontoPorValor);
		
		BigDecimal pesoTotal = BigDecimal.ZERO;
		BigDecimal taxaFragilTotal = BigDecimal.ZERO;
		
		for (ItemCompra item : carrinho.getItens()) {
			Produto produto = item.getProduto();
			Long quantidade = item.getQuantidade();
			
			BigDecimal pesoCubico = produto.getComprimento()
					.multiply(produto.getLargura())
					.multiply(produto.getAltura())
					.divide(FATOR_PESO_CUBICO, 2, RoundingMode.HALF_UP);
			
			BigDecimal pesoTributavelUnitario = produto.getPesoFisico().max(pesoCubico);
			
			pesoTotal = pesoTotal.add(pesoTributavelUnitario.multiply(new BigDecimal(quantidade)));
			
			if (produto.isFragil()) {
				taxaFragilTotal = taxaFragilTotal.add(FRETE_TAXA_FRAGIL.multiply(new BigDecimal(quantidade)));
			}
		}
		
		BigDecimal freteBase = BigDecimal.ZERO;
		BigDecimal valorPorKg = BigDecimal.ZERO;
		
		if (pesoTotal.compareTo(FAIXA_A_LIMITE) <= 0) {
			valorPorKg = BigDecimal.ZERO; // Isento
		} else if (pesoTotal.compareTo(FAIXA_B_LIMITE) <= 0) {
			valorPorKg = FAIXA_B_VALOR_KG;
		} else if (pesoTotal.compareTo(FAIXA_C_LIMITE) <= 0) {
			valorPorKg = FAIXA_C_VALOR_KG;
		} else {
			valorPorKg = FAIXA_D_VALOR_KG;
		}
		
		if (valorPorKg.compareTo(BigDecimal.ZERO) > 0) {
			freteBase = pesoTotal.multiply(valorPorKg).add(FRETE_TAXA_MINIMA);
		}
		
		freteBase = freteBase.add(taxaFragilTotal);
		
		BigDecimal multiplicadorRegiao = getMultiplicadorRegiao(regiao);
		BigDecimal freteComRegiao = freteBase.multiply(multiplicadorRegiao);
		
		BigDecimal freteFinal = freteComRegiao;
		
		if (tipoCliente == TipoCliente.OURO) {
			freteFinal = BigDecimal.ZERO;
		} else if (tipoCliente == TipoCliente.PRATA) {
			freteFinal = freteComRegiao.multiply(new BigDecimal("0.50"));
		}
		
		BigDecimal total = subtotalComDesconto.add(freteFinal);
		
		return total.setScale(2, RoundingMode.HALF_UP);
	}
	
		private BigDecimal getMultiplicadorRegiao(Regiao regiao) {
			switch (regiao) {
				case SUDESTE:
					return new BigDecimal("1.00");
				case SUL:
					return new BigDecimal("1.05");
				case NORDESTE:
					return new BigDecimal("1.10");
				case CENTRO_OESTE:
					return new BigDecimal("1.20");
				case NORTE:
					return new BigDecimal("1.30");
				default:
					return new BigDecimal("1.00");
			}
		}
}

