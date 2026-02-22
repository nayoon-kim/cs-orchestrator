package io.nayoon.cs_orchestrator.repository;

import io.nayoon.cs_orchestrator.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
