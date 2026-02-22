package io.nayoon.cs_orchestrator.controller;

import io.nayoon.cs_orchestrator.domain.Channel;
import io.nayoon.cs_orchestrator.domain.TicketStatus;
import io.nayoon.cs_orchestrator.service.TicketCommandService;
import jakarta.validation.constraints.NotBlank;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketCommandService ticketCommandService;

    public TicketController(TicketCommandService ticketCommandService) {
        this.ticketCommandService = ticketCommandService;
    }

    @PostMapping
    public ResponseEntity<CreateTicketResponse> create(@RequestBody CreateTicketRequest req) {
        var result = ticketCommandService.createTicket(req.customerId(), req.channel(), req.content());
        return ResponseEntity.ok(new CreateTicketResponse(result.ticketId(), result.messageId(), TicketStatus.NEW.toString(), result.due()));
    }

    public record CreateTicketRequest(@NotNull Long customerId,
                                      @NotNull Channel channel,
                                      @NotBlank String content) {}
    public record CreateTicketResponse(Long ticketId,
                                       Long messageId,
                                       String status,
                                       Instant firstResponseDue) {}
}
