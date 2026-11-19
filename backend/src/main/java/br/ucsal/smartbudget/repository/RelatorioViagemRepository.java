package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.RelatorioViagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RelatorioViagemRepository extends JpaRepository<RelatorioViagem, Long> {
    Optional<RelatorioViagem> findByViagemId(Long viagemId);
}
