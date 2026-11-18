package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.DicaEconomia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DicaEconomiaRepository extends JpaRepository<DicaEconomia, Long> {
    List<DicaEconomia> findByViagemId(Long viagemId);
}
