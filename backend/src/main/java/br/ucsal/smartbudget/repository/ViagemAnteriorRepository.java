package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.ViagemAnterior;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ViagemAnteriorRepository extends JpaRepository<ViagemAnterior, Long> {
    List<ViagemAnterior> findByViajanteIdOrderByDataInicioDesc(Long viajanteId);

    Optional<ViagemAnterior> findByIdAndViajanteId(Long id, Long viajanteId);
}
