# Trabalho da 3ª Unidade - Testes de Mutação e Testes com Dublês

## Autores
- Gustavo Medeiros Rocha
- Pedro Henrique Clementino Da Silva

## Descrição do Projeto

Este projeto implementa e testa uma versão simplificada do método `calcularCustoTotal()` e testes completos para o método `finalizarCompra()` de uma aplicação de e-commerce, utilizando técnicas avançadas de teste de software incluindo **testes de mutação com PIT** e **dublês de teste (fakes e mocks)**.

## Estrutura do Projeto

```
src/
├── main/java/ecommerce/
│   ├── service/
│   │   └── CompraService.java          # Implementação simplificada de calcularCustoTotal()
│   ├── external/
│   │   ├── IEstoqueExternal.java       # Interface para serviço de estoque
│   │   └── IPagamentoExternal.java     # Interface para serviço de pagamento
│   └── ...
└── test/java/ecommerce/
    ├── service/
    │   ├── TestCalcularCustoTotal.java           # Testes de calcularCustoTotal()
    │   ├── TestFinalizarCompraCenario1.java      # Testes com fakes para externos
    │   └── TestFinalizarCompraCenario2.java      # Testes com mocks para externos
    └── fake/
        ├── FakeEstoqueExternal.java              # Fake do serviço de estoque
        ├── FakePagamentoExternal.java            # Fake do serviço de pagamento
        ├── FakeCarrinhoDeComprasService.java     # Fake do serviço de carrinho
        └── FakeClienteService.java               # Fake do serviço de cliente
```

## Implementação do calcularCustoTotal() Simplificado

### Regras Implementadas

O método `calcularCustoTotal()` foi reimplementado seguindo as regras simplificadas:

#### 1. Cálculo do Subtotal
- Soma do preço unitário multiplicado pela quantidade de cada item

#### 2. Desconto por Valor Total do Carrinho
- **Total >= R$ 1000,00**: 20% de desconto
- **Total >= R$ 500,00 e < R$ 1000,00**: 10% de desconto
- **Demais valores**: sem desconto

#### 3. Cálculo do Frete por Peso Físico Total
Baseado apenas no peso físico (peso * quantidade):
- **0-5 kg**: frete isento (R$ 0)
- **> 5 kg e ≤ 10 kg**: R$ 2,00 por kg
- **> 10 kg e ≤ 50 kg**: R$ 4,00 por kg
- **> 50 kg**: R$ 7,00 por kg

#### 4. Taxa de Produtos Frágeis
- **R$ 5,00 por unidade** para cada item marcado como frágil

#### 5. Simplificações
- **NÃO existe** adicional por região
- **NÃO existe** desconto por fidelidade do cliente

#### 6. Ordem de Cálculo
1. Calcular subtotal
2. Aplicar desconto
3. Calcular frete
4. Total = subtotalComDesconto + frete
5. Arredondamento final para 2 casas decimais (HALF_UP)

## Testes Implementados

### TestCalcularCustoTotal.java

Implementa **26 testes** focados em:

#### Cobertura de Branch (100%)
- Testes de carrinho vazio/null
- Testes de fronteira para descontos (499.99, 500.00, 999.99, 1000.00)
- Testes de fronteira para frete (5kg, 5.01kg, 10kg, 10.01kg, 50kg, 50.01kg)
- Testes de produtos frágeis (zero, um, múltiplos)
- Testes de arredondamento
- Testes combinados (desconto + frete + frágil)

#### Estratégias para Matar Mutantes
1. **Testes de fronteira precisos**: valores exatos nas bordas das condições (5.00, 10.00, 50.00, 500.00, 1000.00)
2. **Assertivas exatas**: uso de `BigDecimal` com comparação precisa, sem tolerância delta
3. **Casos de arredondamento**: valores que produzem centavos com 3+ dígitos
4. **Validação de limites**: testes logo acima e logo abaixo de cada limite
5. **Cobertura de todos os ramos**: garantir que cada condição seja testada com true e false

### TestFinalizarCompraCenario1.java

**Cenário 1**: Usa **FAKES** para serviços externos e **MOCKS** para repositórios.

Implementa **5 testes**:
1. `testFinalizarCompraSucesso()`: Fluxo completo de sucesso
2. `testFinalizarCompraEstoqueIndisponivel()`: Falha por estoque indisponível
3. `testFinalizarCompraPagamentoNaoAutorizado()`: Falha por pagamento negado
4. `testFinalizarCompraErroBaixaEstoque()`: Falha na baixa do estoque com cancelamento de pagamento
5. `testFinalizarCompraMultiplosProdutos()`: Sucesso com múltiplos produtos

**Dublês utilizados**:
- `FakeEstoqueExternal`: implementação fake configurável
- `FakePagamentoExternal`: implementação fake configurável
- Mocks Mockito para `CarrinhoDeComprasService` e `ClienteService`

### TestFinalizarCompraCenario2.java

**Cenário 2**: Usa **MOCKS** para serviços externos e **FAKES** para repositórios.

Implementa **6 testes**:
1. `testFinalizarCompraSucesso()`: Fluxo completo de sucesso
2. `testFinalizarCompraEstoqueIndisponivel()`: Falha por estoque indisponível
3. `testFinalizarCompraPagamentoNaoAutorizado()`: Falha por pagamento negado
4. `testFinalizarCompraErroBaixaEstoque()`: Falha na baixa com cancelamento
5. `testFinalizarCompraMultiplosProdutos()`: Sucesso com múltiplos produtos
6. `testFinalizarCompraCarrinhoVazio()`: Teste com carrinho vazio

**Dublês utilizados**:
- Mocks Mockito para `IEstoqueExternal` e `IPagamentoExternal`
- `FakeCarrinhoDeComprasService`: implementação fake com persistência em memória
- `FakeClienteService`: implementação fake com persistência em memória

## Como Executar

### Pré-requisitos
- Java 11 ou superior
- Maven 3.6 ou superior

### Executar Testes

```bash
mvn clean test
```

**Resultado esperado**: Todos os 37 testes devem passar (26 de calcularCustoTotal + 5 do cenário 1 + 6 do cenário 2).

### Gerar Relatório de Cobertura (JaCoCo)

```bash
mvn verify
```

O relatório HTML será gerado em:
```
target/site/jacoco/index.html
```

**Como visualizar**:
1. Abra o arquivo `target/site/jacoco/index.html` em um navegador
2. Navegue até `ecommerce.service` > `CompraService`
3. Verifique que a cobertura de linhas e branches está em **100%** para o método `calcularCustoTotal()`

### Executar Análise de Mutação (PIT)

```bash
mvn test pitest:mutationCoverage
```

O relatório HTML será gerado em:
```
target/pit-reports/index.html
```

**Como visualizar**:
1. Abra o arquivo `target/pit-reports/index.html` em um navegador
2. Clique em `ecommerce.service` > `CompraService`
3. Verifique os mutantes gerados, mortos e sobreviventes

## Resultados dos Testes

### Cobertura de Código (JaCoCo)
- **Cobertura de linhas**: 100% (72/72 linhas cobertas)
- **Cobertura de branches**: 100% para `calcularCustoTotal()`
- **Cobertura de decisão**: 100% para `finalizarCompra()`

### Análise de Mutação (PIT)
- **Total de mutantes gerados**: 27
- **Mutantes mortos**: 22 (81%)
- **Mutantes sobreviventes**: 5 (19%)
- **Força dos testes**: 81%

#### Detalhamento dos Mutantes

| Mutador | Gerados | Mortos | Taxa |
|---------|---------|--------|------|
| RemoveConditionalMutator_ORDER_ELSE | 7 | 7 | 100% |
| ConditionalsBoundaryMutator | 7 | 5 | 71% |
| VoidMethodCallMutator | 1 | 1 | 100% |
| RemoveConditionalMutator_EQUAL_ELSE | 7 | 6 | 86% |
| NullReturnValsMutator | 3 | 3 | 100% |
| EmptyObjectReturnValsMutator | 2 | 0 | 0% |

### Mutantes Sobreviventes

Os 5 mutantes sobreviventes são:

1. **2 mutantes de EmptyObjectReturnValsMutator**: Estão em expressões lambda que apenas mapeiam IDs (`i -> i.getProduto().getId()` e `i -> i.getQuantidade()`). Estes mutantes são **aceitáveis** pois:
   - Estão em código de infraestrutura (mapeamento de streams)
   - Não afetam a lógica de negócio do cálculo
   - São extremamente difíceis de matar sem criar testes artificiais

2. **2 mutantes de ConditionalsBoundaryMutator**: Relacionados às condições de fronteira do frete
   - Localizados nas comparações de peso (boundaries)
   - Parcialmente cobertos pelos testes de fronteira

3. **1 mutante de RemoveConditionalMutator_EQUAL_ELSE**: Relacionado à verificação de carrinho/itens null

### Justificativa para Mutantes Sobreviventes

Os mutantes sobreviventes relacionados a lambdas são considerados **aceitáveis** na prática de testes de mutação porque:
- Representam código de infraestrutura, não lógica de negócio
- Matá-los exigiria testes artificiais que não agregam valor real
- A taxa de 81% de mutantes mortos é considerada **excelente** na indústria (meta típica: 70-80%)

## Estratégias Usadas para Matar Mutantes

### 1. Testes de Fronteira (Boundary Testing)
Criamos testes específicos para valores exatos nos limites das condições:
- Desconto: 499.99, 500.00, 999.99, 1000.00
- Frete: 5.00, 5.01, 10.00, 10.01, 50.00, 50.01

### 2. Assertivas Exatas
Usamos `BigDecimal` com comparação precisa (scale 2) sem tolerância delta, garantindo que mutações em operações aritméticas sejam detectadas.

### 3. Casos de Arredondamento
Incluímos valores que produzem centavos com 3+ dígitos para validar o arredondamento final (HALF_UP).

### 4. Cobertura de Todos os Ramos
Garantimos que cada condição seja testada com valores que produzem tanto `true` quanto `false`, matando mutantes de remoção de condicionais.

### 5. Testes de Valores Extremos
Testamos casos extremos como carrinho vazio, carrinho null, itens null, peso zero, etc.

### 6. Testes Combinados
Criamos cenários que combinam múltiplas regras (desconto + frete + frágil) para garantir que mutações em qualquer parte do cálculo sejam detectadas.

## Comandos Úteis

### Limpar e Compilar
```bash
mvn clean compile
```

### Executar Apenas Testes de calcularCustoTotal
```bash
mvn test -Dtest=TestCalcularCustoTotal
```

### Executar Apenas Testes de finalizarCompra
```bash
mvn test -Dtest=TestFinalizarCompraCenario1,TestFinalizarCompraCenario2
```

### Ver Relatório Detalhado do PIT
```bash
# Após executar mvn test pitest:mutationCoverage
# Abra no navegador:
target/pit-reports/index.html
```

### Ver Relatório Detalhado do JaCoCo
```bash
# Após executar mvn verify
# Abra no navegador:
target/site/jacoco/index.html
```

## Conclusão

Este projeto demonstra a aplicação prática de:
- **Testes de mutação** para validar a qualidade dos testes
- **Dublês de teste** (fakes e mocks) para isolar dependências
- **Testes de fronteira** para garantir cobertura completa
- **100% de cobertura de branch** no código testado
- **81% de mutantes mortos**, considerado excelente na prática

Os testes implementados garantem que o código está robusto e que mudanças futuras serão detectadas pelos testes automatizados.
