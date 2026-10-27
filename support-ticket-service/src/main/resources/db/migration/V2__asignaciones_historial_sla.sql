-- V2__asignaciones_historial_sla.sql: Historial de tickets e índices de SLA

CREATE TABLE ticket_histories (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    modified_by_id BIGINT NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    observations TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_histories_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_histories_modifier FOREIGN KEY (modified_by_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Índices para optimización de consultas, auditoría y cumplimiento de SLA
CREATE INDEX idx_tickets_requester ON tickets(requester_id);
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_sla_status ON tickets(sla_deadline, status);
CREATE INDEX idx_histories_ticket ON ticket_histories(ticket_id);
CREATE INDEX idx_histories_modified_by ON ticket_histories(modified_by_id);
