package io.nayoon.cs_orchestrator.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status; // NEW, TRIAGED, QUEUED

    @Enumerated(EnumType.STRING)
    @Column(name = "routing_decision", nullable = false)
    private RoutingDecision routingDecision; // AUTO, DRAFT, HUMAN

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(name = "customer_id", nullable = false)
    private Long customerId; // DB가 VARCHAR라서 우선 String

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "triaged_at")
    private Instant triagedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String triageResultJson;

    protected Ticket() {}

    private Ticket(Long customerId, Channel channel) {
        this.customerId = customerId;
        this.channel = channel;
        this.status = TicketStatus.NEW;
        this.routingDecision = RoutingDecision.NEW;
        this.priority = 3;
        this.createdAt = Instant.now();
    }

    public static Ticket open(Long customerId, Channel channel) {
        return new Ticket(customerId, channel);
    }

    public Long getId() {
        return id;
    }
    public Channel getChannel() { return channel; }
    public TicketStatus getStatus() { return status; }
    public Long getCustomerId() { return customerId; }
}
