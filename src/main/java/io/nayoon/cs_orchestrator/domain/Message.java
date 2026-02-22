package io.nayoon.cs_orchestrator.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "message")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    @Column(columnDefinition = "text")
    private String content;
    private Instant createdAt;

    protected Message() {}

    private Message(Long ticketId, String content) {
        this.ticketId = ticketId;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public static Message create(Long ticketId, String content) {
        return new Message(ticketId, content);
    }

    public Long getId() {
        return id;
    }
}
