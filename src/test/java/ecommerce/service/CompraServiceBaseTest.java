package ecommerce.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

/**
 * Classe base para os testes de CompraService.
 * Configura o serviço e constantes comuns.
 */
public abstract class CompraServiceBaseTest {

    @InjectMocks
    protected CompraService compraService;

    @Mock
    protected CarrinhoDeComprasService carrinhoService;
    @Mock
    protected ClienteService clienteService;
    @Mock
    protected IEstoqueExternal estoqueExternal;
    @Mock
    protected IPagamentoExternal pagamentoExternal;
    
    protected static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    protected static final BigDecimal UM_CENTAVO = new BigDecimal("0.01");
    protected static final BigDecimal LIMITE_DESC_10_SUP = new BigDecimal("500.00");
    protected static final BigDecimal LIMITE_DESC_20_SUP = new BigDecimal("1000.00");
    protected static final BigDecimal LIMITE_PESO_A = new BigDecimal("5.00");
    protected static final BigDecimal LIMITE_PESO_B = new BigDecimal("10.00");
    protected static final BigDecimal LIMITE_PESO_C = new BigDecimal("50.00");
    protected static final BigDecimal TAXA_MINIMA_FRETE = new BigDecimal("12.00");
    protected static final BigDecimal TAXA_FRAGIL = new BigDecimal("5.00");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}

