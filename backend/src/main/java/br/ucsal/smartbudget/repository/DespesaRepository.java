package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    List<Despesa> findByCategoriaOrcamentoViagemIdOrderByDataDescIdDesc(Long viagemId);

    Optional<Despesa> findByIdAndCategoriaOrcamentoViagemId(Long id, Long viagemId);
}
