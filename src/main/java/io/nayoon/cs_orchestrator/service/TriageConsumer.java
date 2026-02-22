package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.ai.OpenAiTriageClient;
import io.nayoon.cs_orchestrator.domain.*;
import io.nayoon.cs_orchestrator.repository.TicketRepository;
import io.nayoon.cs_orchestrator.repository.TriageRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class TriageConsumer {
    private final TriageRepository triageRepository;
    private final TicketRepository ticketRepository;
    private final OpenAiTriageClient openAiTriageClient;
    private final RedisQueue redisQueue;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TriageConsumer(TriageRepository triageRepository, TicketRepository ticketRepository,
                          OpenAiTriageClient openAiTriageClient, RedisQueue redisQueue) {
        this.triageRepository = triageRepository;
        this.ticketRepository = ticketRepository;
        this.openAiTriageClient = openAiTriageClient;
        this.redisQueue = redisQueue;
    }

    @KafkaListener(topics = "ticket.created", groupId = "triage-consumer")
    public void onMessage(TicketCreatedEvent event) {
        handle(event.ticketId());
    }

    @Transactional
    public void handle(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (!isNew(ticket.getStatus())) return;

        String message = triageRepository.findMessageContent(ticketId);

        TriageResult triage = openAiTriageClient
                .triage(ticketId.toString(), ticket.getChannel().name(), ticket.getCustomerId(), message)
                .block();

        RoutingDecision decision = RoutingPolicy.hardGate(triage);
        int priority = PriorityScoring.scorePriority(triage, decision);

        String triageJson;
        try {
            triageJson = objectMapper.writeValueAsString(triage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int updated = triageRepository.conditionalUpdateQueued(ticketId, decision, priority, triageJson);
        if (updated == 0) {
            // 누군가 먼저 처리했거나 status 조건 불일치
            return;
        }

        // 업데이트 성공했을 때만 enqueue
        redisQueue.enqueueGlobal(ticketId);
    }

    private boolean isNew(TicketStatus status) {
        return TicketStatus.NEW == status;
    }
}
