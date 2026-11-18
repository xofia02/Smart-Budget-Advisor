package br.ucsal.smartbudget.repository;

import br.ucsal.smartbudget.model.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {
    List<MensagemChat> findByViagemIdOrderByEnviadaEmAsc(Long viagemId);
}
