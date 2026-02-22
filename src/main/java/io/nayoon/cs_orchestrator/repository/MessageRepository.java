package io.nayoon.cs_orchestrator.repository;

import io.nayoon.cs_orchestrator.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}
