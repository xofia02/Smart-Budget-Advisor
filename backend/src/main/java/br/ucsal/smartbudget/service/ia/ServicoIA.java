package br.ucsal.smartbudget.service.ia;

import br.ucsal.smartbudget.model.DicaEconomia;
import br.ucsal.smartbudget.model.PrevisaoCusto;
import br.ucsal.smartbudget.model.Viagem;

import java.util.List;

public interface ServicoIA {
    PrevisaoCusto preverCustos(Viagem viagem);

    List<DicaEconomia> gerarDicas(Viagem viagem);

    String responderChat(String pergunta, Viagem contexto);
}
