# Trabalho de Teste de Software - Análise e Implementação de Testes (JUnit 5 e JaCoCo)

Este trabalho consiste na implementação do método `calcularCustoTotal` na classe `CompraService.java` e na criação de um conjunto abrangente de testes de software para garantir sua correção e robustez, conforme os critérios de Caixa Preta e Caixa Branca.

---

## 1. Implementação do Método `calcularCustoTotal`

O método foi implementado na classe `ecommerce.service.CompraService` seguindo as regras de negócio:

1.  **Cálculo do Peso Tributável:** `max(peso_fisico, peso_cubico)`.
2.  **Desconto por Quantidade/Tipo de Item:** 0%, 5%, 10% ou 15% aplicado ao subtotal do tipo de produto.
3.  **Desconto por Valor Total:** 0%, 10% ou 20% aplicado ao subtotal restante.
4.  **Cálculo do Frete Base:** Baseado no peso tributável (4 faixas de preço por kg + R$12 de taxa mínima).
5.  **Taxa de Fragilidade:** R$5.00 por item frágil.
6.  **Multiplicador Regional:** Aplicado ao frete base.
7.  **Desconto por Nível de Cliente:** 0%, 50% ou 100% aplicado ao frete final.

## 2. Testes de Caixa Preta

Os testes de Caixa Preta foram implementados nas seguintes classes, cobrindo os critérios exigidos:

| Critério de Teste | Classe de Teste | Cobertura |
| :--- | :--- | :--- |
| Particionamento de Equivalência | `CompraServiceParticaoTest.java` | Cobertura de todas as partições de Desconto por Quantidade, Desconto por Valor e Faixas de Frete. |
| Análise de Valores Limites | `CompraServiceLimiteTest.java` | Cobertura dos limites superiores e inferiores das faixas de Desconto por Quantidade e Faixas de Frete. |
| Tabela de Decisão | `CompraServiceDecisaoTest.java` | Cobertura de regras compostas de Frete (Peso + Fragilidade + Região + Nível Cliente) e Acumulação de Descontos. |

---

## 3. Testes de Caixa Branca e Robustez

Os testes de Caixa Branca e Robustez foram implementados na classe `CompraServiceWhiteBoxTest.java`.

### 3.1. Grafo de Fluxo de Controle e Complexidade Ciclomática

O método `calcularCustoTotal` é a principal unidade de teste. O grafo de fluxo de controle (GFC) e a Complexidade Ciclomática (CC) foram calculados para o método.

**Complexidade Ciclomática (CC):**

A Complexidade Ciclomática foi calculada pela fórmula `CC = P + 1`, onde `P` é o número de predicados (decisões) no código.

| Predicado (Decisão) | Descrição |
| :--- | :--- |
| `carrinho == null || carrinho.getItens() == null || carrinho.getItens().isEmpty()` | Verifica se o carrinho é nulo ou vazio. |
| `item.getQuantidade() <= 0` | Robustez: Quantidade negativa/zero. |
| `item.getProduto().getPreco().compareTo(BigDecimal.ZERO) < 0` | Robustez: Preço negativo. |
| `totalItensDoTipo >= 3 && totalItensDoTipo <= 4` | Desconto por Tipo (5%). |
| `totalItensDoTipo >= 5 && totalItensDoTipo <= 7` | Desconto por Tipo (10%). |
| `totalItensDoTipo >= 8` | Desconto por Tipo (15%). |
| `percentualDesconto.compareTo(BigDecimal.ZERO) > 0` | Verifica se há desconto por tipo. |
| `subtotalComDesconto.compareTo(new BigDecimal("500.00")) > 0 && subtotalComDesconto.compareTo(new BigDecimal("1000.00")) <= 0` | Desconto por Valor (10%). |
| `subtotalComDesconto.compareTo(new BigDecimal("1000.00")) > 0` | Desconto por Valor (20%). |
| `pesoTributavel.compareTo(FAIXA_A_LIMITE) > 0 && pesoTributavel.compareTo(FAIXA_B_LIMITE) <= 0` | Faixa de Frete B. |
| `pesoTributavel.compareTo(FAIXA_B_LIMITE) > 0 && pesoTributavel.compareTo(FAIXA_C_LIMITE) <= 0` | Faixa de Frete C. |
| `pesoTributavel.compareTo(FAIXA_C_LIMITE) > 0` | Faixa de Frete D. |
| `item.getProduto().isFragil()` | Aplica taxa de fragilidade. |
| `multiplicadorRegiao.compareTo(BigDecimal.ZERO) > 0` | Verifica se há multiplicador de região. |
| `tipoCliente == TipoCliente.PRATA` | Desconto de Frete (50%). |
| `tipoCliente == TipoCliente.OURO` | Desconto de Frete (100%). |

**Total de Predicados (P) = 16**

**Complexidade Ciclomática (CC) = 16 + 1 = 17**

### 3.2. Cobertura de Arestas (Branch Coverage)

A cobertura de arestas foi alcançada com sucesso, garantindo que todos os caminhos de decisão (ramos `if/else`, `switch` cases) foram executados.

**Resultado JaCoCo para `CompraService`:**

| Métrica | Perdido | Coberto | Cobertura |
| :--- | :--- | :--- | :--- |
| Instruções | 121 | 460 | **79%** |
| Ramos (Branches) | 0 | 49 | **100%** |
| Linhas | 9 | 101 | **92%** |
| Métodos | 3 | 33 | **91%** |
| Classes | 0 | 8 | **100%** |

### 3.3. Cobertura MC/DC (Modified Condition/Decision Coverage)

O critério MC/DC foi aplicado à decisão composta mais complexa no cálculo do frete:

**Decisão:** Faixa de Frete B: `(pesoTotal > 5.00) && (pesoTotal <= 10.00)`

| Condição | Variável | Descrição |
| :--- | :--- | :--- |
| **C1** | `pesoTotal > 5.00` | Peso total maior que o limite da Faixa A. |
| **C2** | `pesoTotal <= 10.00` | Peso total menor ou igual ao limite da Faixa B. |

**Casos de Teste MC/DC:**

| Caso | C1 | C2 | Decisão | Resultado |
| :--- | :--- | :--- | :--- | :--- |
| **MC/DC-1** | T | T | T | **Peso 7.00kg** (Caso base da Faixa B) |
| **MC/DC-2** | F | T | F | **Peso 5.00kg** (C1 altera a decisão - Frete Faixa A) |
| **MC/DC-3** | T | F | F | **Peso 10.01kg** (C2 altera a decisão - Frete Faixa C) |

*Estes testes estão implementados em `CompraServiceWhiteBoxTest.java` nos métodos `calcularCustoTotal_mcdc1_peso7kg`, `calcularCustoTotal_mcdc2_peso5kg` e `calcularCustoTotal_mcdc3_peso10_01kg`.*

### 3.4. Testes de Robustez

Os testes de robustez foram implementados para garantir o tratamento de entradas inválidas:

| Teste | Entrada Inválida | Resultado Esperado |
| :--- | :--- | :--- |
| `R1` | Carrinho Nulo | Retorna `0.00` |
| `R2` | Lista de Itens Nula | Retorna `0.00` |
| `R3` | Lista de Itens Vazia | Retorna `0.00` |
| `R4` | Quantidade de Item Negativa | Lança `IllegalArgumentException` |
| `R5` | Preço de Produto Negativo | Lança `IllegalArgumentException` |

## 4. Instruções de Execução

Este projeto utiliza **Maven** como gerenciador de build e **JUnit 5** para execução dos testes automatizados.  
Os testes podem ser executados de três formas principais:

---

### Método 1 — Executando Testes pelo Lifecycle do Maven (no IntelliJ IDEA)

1. Abra o projeto no **IntelliJ IDEA**.  
2. No painel lateral direito, localize a aba **Maven**.  
3. Expanda o ciclo de vida (**Lifecycle**) do projeto.  
4. Clique em **verify** (ou **test**, se preferir executar apenas os testes).  


### Método 2 — Criando uma Run Configuration de Teste (JUnit) no IntelliJ IDEA

1. No menu superior, vá em **Run → Edit Configurations...**  
2. Clique em **+ → JUnit** para criar uma nova configuração.  
3. Configure:
   - **Build and run:**
   - *Java 11*  
   - *All in package*  
   - **Package:** `ecommerce`  
   - **Working directory:** *$MODULE_WORKING_DIR$*
4. Clique em **Apply → Run** ✅  

---

### Método 3 — Executando Testes via Linha de Comando (Maven CLI)

Se preferir rodar os testes sem IDE:

1. Abra o terminal na raiz do projeto.  
2. Execute um dos comandos abaixo:

```bash
# Executa todos os testes do projeto
mvn test

# Executa testes e verifica a cobertura configurada no JaCoCo
mvn verify

# Executa apenas uma classe de teste específica
mvn -Dtest=CompraServiceWhiteBoxTest test

# Executa um método de teste específico dentro de uma classe
mvn -Dtest=CompraServiceWhiteBoxTest#calcularCustoTotal_mcdc1_peso7kg test
```
---

### Requisitos:

**Java:** *11* 
**Maven**: *3.6+*
**JUnit**: *5.x*
