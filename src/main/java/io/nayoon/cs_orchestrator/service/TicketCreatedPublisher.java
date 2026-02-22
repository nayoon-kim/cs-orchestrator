package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.TicketCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TicketCreatedPublisher {
    private final KafkaTemplate<String, TicketCreatedEvent> kafka;

    public TicketCreatedPublisher(KafkaTemplate<String, TicketCreatedEvent> kafka) {
        this.kafka = kafka;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(TicketCreatedEvent event) {
        String key = String.valueOf(event.ticketId());
        kafka.send("ticket.created", key, event);
    }
}
