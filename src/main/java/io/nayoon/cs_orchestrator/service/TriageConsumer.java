package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TriageConsumer {

    private final TriageService triageService;

    public TriageConsumer(TriageService triageService) {
        this.triageService = triageService;
    }

    @KafkaListener(topics = "ticket.created", groupId = "triage-consumer")
    public void onMessage(TicketCreatedEvent event) {
        triageService.handle(event.ticketId());
    }
}
