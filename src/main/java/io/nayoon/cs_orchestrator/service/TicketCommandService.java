package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.Channel;
import io.nayoon.cs_orchestrator.domain.Message;
import io.nayoon.cs_orchestrator.domain.Ticket;
import io.nayoon.cs_orchestrator.domain.TicketCreatedEvent;
import io.nayoon.cs_orchestrator.repository.MessageRepository;
import io.nayoon.cs_orchestrator.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TicketCommandService {
    private static final Duration FIRST_RESPONSE_SLA = Duration.ofSeconds(120);

    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final SlaService slaService;
    private final ApplicationEventPublisher eventPublisher;

    public TicketCommandService(TicketRepository ticketRepository, MessageRepository messageRepository,
                                SlaService slaService, ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.slaService = slaService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateResult createTicket(Long customerId, Channel channel, String content) {
        // 티켓 생성
        Ticket ticket = ticketRepository.save(Ticket.open(customerId, channel));
        // 메시지 저장
        Message message = messageRepository.save(Message.create(ticket.getId(), content));

        // Redis SLA 등록
        Instant due = Instant.now().plus(FIRST_RESPONSE_SLA);
        slaService.registerFirstResponseSla(ticket.getId(), due);

        // kafka 등록
        eventPublisher.publishEvent(new TicketCreatedEvent(ticket.getId(), message.getId()));

        return new CreateResult(ticket.getId(), message.getId(), due);
    }

    public record CreateResult(Long ticketId, Long messageId, Instant due) {}
}
