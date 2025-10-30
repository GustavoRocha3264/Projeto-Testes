package ecommerce.dto;

public class CompraDTO
{
	private Boolean sucesso;
	private Long transacaoPagamentoId;
	private String mensagem;

	public CompraDTO(Boolean sucesso, Long transacaoPagamentoId, String mensagem)
	{
		this.sucesso = sucesso;
		this.transacaoPagamentoId = transacaoPagamentoId;
		this.mensagem = mensagem;
	}

	public Boolean sucesso()
	{
		return sucesso;
	}

	public Long transacaoPagamentoId()
	{
		return transacaoPagamentoId;
	}

	public String mensagem()
	{
		return mensagem;
	}
}

