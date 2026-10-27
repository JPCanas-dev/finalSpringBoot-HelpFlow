-- V1__schema.sql: Creación de tablas base (users y tickets)

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    location VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    salary DECIMAL(10,2),
    affiliation_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    assignee_id BIGINT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    location VARCHAR(50) NOT NULL,
    affected_asset VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sla_deadline TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_tickets_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_assignee FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
);
