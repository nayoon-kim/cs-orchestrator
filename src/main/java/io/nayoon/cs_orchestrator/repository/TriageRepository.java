package io.nayoon.cs_orchestrator.repository;

import io.nayoon.cs_orchestrator.domain.RoutingDecision;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class TriageRepository {
    private final JdbcTemplate jdbc;

    public TriageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TicketRow findTicketForUpdate(Long ticketId) {
        return jdbc.queryForObject(
                "SELECT id, status, triaged_at FROM ticket WHERE id = ?",
                (rs, rowNum) -> new TicketRow(
                        rs.getLong("id"),
                        rs.getString("status"),
                        rs.getTimestamp("triaged_at") == null ? null : rs.getTimestamp("triaged_at").toInstant()
                ),
                ticketId
        );
    }

    public String findMessageContent(Long ticketId) {
        return jdbc.query(
                "SELECT content FROM message WHERE ticket_id = ?",
                rs -> rs.next() ? rs.getString("content") : null,
                ticketId
        );
    }

    public int conditionalUpdateQueued(Long ticketId, RoutingDecision decision, int priority, String triageResultJson) {
        Timestamp now = Timestamp.from(Instant.now());

        // status 조건은 너 enum에 맞게 조정
        return jdbc.update(
                """
                UPDATE ticket
                SET status = 'QUEUED',
                    routing_decision = ?,
                    priority = ?,
                    triaged_at = ?,
                    triage_result_json = ?::jsonb
                WHERE id = ?
                  AND triaged_at IS NULL
                  AND status NOT IN ('QUEUED','DONE','CLOSED')
                """,
                decision.name(), priority, now, triageResultJson, ticketId
        );
    }

    public record TicketRow(Long ticketId, String status, Instant triagedAt) {}
}
