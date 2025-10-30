package ecommerce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ItemCompra
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne // Vários itens podem se referir ao mesmo produto
	@JoinColumn(name = "produto_id")
	private Produto produto;

	private Long quantidade;

	public ItemCompra()
	{
	}

	public ItemCompra(Long id, Produto produto, Long quantidade)
	{
		this.id = id;
		this.produto = produto;
		this.quantidade = quantidade;
	}

	// Getters e Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Produto getProduto()
	{
		return produto;
	}

	public void setProduto(Produto produto)
	{
		this.produto = produto;
	}

	public Long getQuantidade()
	{
		return quantidade;
	}

	public void setQuantidade(Long quantidade)
	{
		this.quantidade = quantidade;
	}
}
