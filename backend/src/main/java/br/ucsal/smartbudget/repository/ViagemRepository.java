package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {
    List<Viagem> findByViajanteIdOrderByDataInicioDesc(Long viajanteId);

    Optional<Viagem> findByIdAndViajanteId(Long id, Long viajanteId);

    long countByDataInicioLessThanEqualAndDataFimGreaterThanEqual(LocalDate hoje1, LocalDate hoje2);
}
