package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByCategoriaOrcamentoViagemIdOrderByGeradoEmDesc(Long viagemId);

    boolean existsByCategoriaOrcamentoIdAndTipo(Long categoriaOrcamentoId, TipoAlerta tipo);
}
