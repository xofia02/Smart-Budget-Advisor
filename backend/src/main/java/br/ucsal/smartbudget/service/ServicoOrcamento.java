package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.OrcamentoDto.ItemDistribuicao;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.CategoriaOrcamento;
import br.ucsal.smartbudget.model.Despesa;
import br.ucsal.smartbudget.model.PrevisaoCategoria;
import br.ucsal.smartbudget.model.PrevisaoCusto;
import br.ucsal.smartbudget.model.RelatorioViagem;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.repository.AlertaRepository;
import br.ucsal.smartbudget.repository.DespesaRepository;
import br.ucsal.smartbudget.repository.PrevisaoCustoRepository;
import br.ucsal.smartbudget.repository.RelatorioViagemRepository;
import br.ucsal.smartbudget.service.ia.ServicoIA;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ServicoOrcamento {
    public static final Map<CategoriaGasto, BigDecimal> PESOS_PADRAO = Map.of(
            CategoriaGasto.HOSPEDAGEM, new BigDecimal("35"),
            CategoriaGasto.ALIMENTACAO, new BigDecimal("25"),
            CategoriaGasto.TRANSPORTE, new BigDecimal("15"),
            CategoriaGasto.PASSEIOS, new BigDecimal("15"),
            CategoriaGasto.COMPRAS, new BigDecimal("5"),
            CategoriaGasto.OUTROS, new BigDecimal("5"));

    private final ServicoIA servicoIA;
    private final PrevisaoCustoRepository previsaoRepository;
    private final DespesaRepository despesaRepository;
    private final AlertaRepository alertaRepository;
    private final RelatorioViagemRepository relatorioRepository;

    public ServicoOrcamento(ServicoIA servicoIA, PrevisaoCustoRepository previsaoRepository,
                            DespesaRepository despesaRepository, AlertaRepository alertaRepository,
                            RelatorioViagemRepository relatorioRepository) {
        this.servicoIA = servicoIA;
        this.previsaoRepository = previsaoRepository;
        this.despesaRepository = despesaRepository;
        this.alertaRepository = alertaRepository;
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional
    public PrevisaoCusto gerarPrevisao(Viagem viagem) {
        PrevisaoCusto previsao = servicoIA.preverCustos(viagem);
        previsao.setViagem(viagem);
        viagem.getPrevisoes().add(previsao);
        return previsaoRepository.save(previsao);
    }

    public Optional<PrevisaoCusto> buscarUltimaPrevisao(Viagem viagem) {
        return previsaoRepository.findFirstByViagemIdOrderByGeradaEmDesc(viagem.getId());
    }

    @Transactional
    public void distribuirOrcamento(Viagem viagem) {
        Map<CategoriaGasto, BigDecimal> pesos = new EnumMap<>(CategoriaGasto.class);
        Optional<PrevisaoCusto> previsao = buscarUltimaPrevisao(viagem);
        if (previsao.isPresent()) {
            for (PrevisaoCategoria item : previsao.get().getItens()) {
                pesos.put(item.getCategoria(), item.getRecomendado());
            }
        } else {
            pesos.putAll(PESOS_PADRAO);
        }

        dividirProporcionalmente(pesos, viagem.getOrcamentoTotal())
                .forEach((categoria, valor) -> viagem.buscarCategoria(categoria).setValorAlocado(valor));
        depoisDeMudarDistribuicao(viagem);
    }

    @Transactional
    public void salvarDistribuicao(Viagem viagem, List<ItemDistribuicao> itens) {
        Set<CategoriaGasto> recebidas = EnumSet.noneOf(CategoriaGasto.class);
        BigDecimal soma = BigDecimal.ZERO;
        for (ItemDistribuicao item : itens) {
            recebidas.add(item.categoria());
            soma = soma.add(item.valorAlocado());
        }
        if (recebidas.size() != CategoriaGasto.values().length) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Envie o valor de cada uma das 6 categorias.");
        }
        if (soma.compareTo(viagem.getOrcamentoTotal()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A soma das categorias (" + reais(soma)
                    + ") ultrapassa o orçamento total da viagem (" + reais(viagem.getOrcamentoTotal()) + ").");
        }

        for (ItemDistribuicao item : itens) {
            viagem.buscarCategoria(item.categoria()).setValorAlocado(item.valorAlocado());
        }
        depoisDeMudarDistribuicao(viagem);
    }

    @Transactional
    public Alerta registrarDespesa(Despesa despesa) {
        CategoriaOrcamento categoria = despesa.getCategoriaOrcamento();
        Viagem viagem = categoria.getViagem();

        if (bloquearSeEstouro(viagem)) {
            throw new ApiException(HttpStatus.LOCKED, "Os gastos passaram de 120% do orçamento. "
                    + "Revise a distribuição do orçamento ou aumente o orçamento total para lançar novas despesas.");
        }
        conferirData(despesa, viagem);

        categoria.adicionarDespesa(despesa);
        despesaRepository.save(despesa);
        return gerarAlerta(categoria);
    }

    public boolean bloquearSeEstouro(Viagem viagem) {
        return viagem.estaBloqueadaPorEstouro();
    }

    @Transactional
    public RelatorioViagem gerarRelatorio(Viagem viagem) {
        RelatorioViagem relatorio = relatorioRepository.findByViagemId(viagem.getId()).orElseGet(RelatorioViagem::new);
        relatorio.preencherCom(viagem);
        viagem.setRelatorio(relatorio);
        return relatorioRepository.save(relatorio);
    }

    @Transactional
    public Alerta gerarAlerta(CategoriaOrcamento categoria) {
        if (!categoria.getViagem().temOrcamentoDistribuido()) {
            return null;
        }
        Alerta alerta = categoria.verificarAlerta();
        if (alerta == null || alertaRepository.existsByCategoriaOrcamentoIdAndTipo(categoria.getId(), alerta.getTipo())) {
            return null;
        }
        categoria.getAlertas().add(alerta);
        return alertaRepository.save(alerta);
    }

    public void conferirData(Despesa despesa, Viagem viagem) {
        if (!despesa.validarData(viagem)) {
            DateTimeFormatter br = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data da despesa precisa estar entre "
                    + viagem.getDataInicio().format(br) + " e " + viagem.getDataFim().format(br) + ".");
        }
    }

    public static Map<CategoriaGasto, BigDecimal> dividirProporcionalmente(Map<CategoriaGasto, BigDecimal> pesos,
                                                                           BigDecimal total) {
        BigDecimal somaPesos = pesos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<CategoriaGasto, BigDecimal> resultado = new EnumMap<>(CategoriaGasto.class);
        BigDecimal distribuido = BigDecimal.ZERO;
        CategoriaGasto maior = CategoriaGasto.HOSPEDAGEM;

        for (CategoriaGasto categoria : CategoriaGasto.values()) {
            BigDecimal peso = somaPesos.signum() == 0 ? BigDecimal.ONE : pesos.getOrDefault(categoria, BigDecimal.ZERO);
            BigDecimal divisor = somaPesos.signum() == 0 ? BigDecimal.valueOf(6) : somaPesos;
            BigDecimal valor = total.multiply(peso).divide(divisor, 2, RoundingMode.DOWN);
            resultado.put(categoria, valor);
            distribuido = distribuido.add(valor);
            if (peso.compareTo(pesos.getOrDefault(maior, BigDecimal.ZERO)) > 0) {
                maior = categoria;
            }
        }

        BigDecimal sobra = total.setScale(2, RoundingMode.DOWN).subtract(distribuido);
        resultado.put(maior, resultado.get(maior).add(sobra));
        return resultado;
    }

    private void depoisDeMudarDistribuicao(Viagem viagem) {
        viagem.registrarRevisaoDoOrcamento();
        for (CategoriaOrcamento categoria : viagem.getCategorias()) {
            gerarAlerta(categoria);
        }
    }

    private String reais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor).replace('\u00a0', ' ');
    }
}
