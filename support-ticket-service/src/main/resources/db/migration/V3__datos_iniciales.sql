-- =========================================================================
-- V3__datos_iniciales.sql: Usuarios y técnicos iniciales de prueba
-- Contraseña para todos los usuarios semilla: 12345
-- Hash BCrypt: $2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm
-- =========================================================================

-- 1. Administrador del Sistema TI
INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1017234890',
    'Juan Morales',
    'admin@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_ADMIN',
    'SEDE_CENTRAL',
    'INFRAESTRUCTURA',
    'ACTIVO',
    5200000.00,
    '2022-01-15',
    CURRENT_TIMESTAMP
);

-- 2. Técnicos de Soporte
INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1020456789',
    'Andres Restrepo',
    'andres@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_TECNICO',
    'SEDE_NORTE',
    'RED',
    'ACTIVO',
    3100000.00,
    '2023-03-01',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1037654321',
    'Santiago Henao',
    'santiago@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_TECNICO',
    'SEDE_CENTRAL',
    'HARDWARE',
    'ACTIVO',
    3050000.00,
    '2023-04-10',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1040987654',
    'Valentina Osorio',
    'valentina@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_TECNICO',
    'SEDE_SUR',
    'SOFTWARE',
    'ACTIVO',
    3300000.00,
    '2023-05-20',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1015678123',
    'Diana Mejia',
    'diana@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_TECNICO',
    'SEDE_CENTRAL',
    'ACCESOS',
    'ACTIVO',
    3000000.00,
    '2023-06-01',
    CURRENT_TIMESTAMP
);

-- 3. Empleados / Usuarios Solicitantes
INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1022345678',
    'Mateo Ramirez',
    'mateo@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_USUARIO',
    'SEDE_CENTRAL',
    'FINANZAS',
    'ACTIVO',
    2850000.00,
    '2024-01-10',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1035890123',
    'Mariana Zapata',
    'mariana@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_USUARIO',
    'SEDE_NORTE',
    'GESTION_HUMANA',
    'ACTIVO',
    2750000.00,
    '2024-02-15',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1042567890',
    'Esteban Correa',
    'esteban@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_USUARIO',
    'SEDE_SUR',
    'OPERACIONES',
    'ACTIVO',
    2600000.00,
    '2024-03-01',
    CURRENT_TIMESTAMP
);

INSERT INTO users (document, name, email, password, role, location, department, status, salary, affiliation_date, created_at)
VALUES (
    '1018901234',
    'Carolina Agudelo',
    'carolina@helpflow.com',
    '$2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm',
    'ROLE_USUARIO',
    'SEDE_CENTRAL',
    'VENTAS',
    'INACTIVO',
    2400000.00,
    '2023-11-01',
    CURRENT_TIMESTAMP
);
