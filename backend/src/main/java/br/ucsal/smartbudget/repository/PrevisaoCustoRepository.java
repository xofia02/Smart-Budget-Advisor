package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.PrevisaoCusto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrevisaoCustoRepository extends JpaRepository<PrevisaoCusto, Long> {
    Optional<PrevisaoCusto> findFirstByViagemIdOrderByGeradaEmDesc(Long viagemId);
}
