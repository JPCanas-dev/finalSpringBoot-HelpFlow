-- V3__datos_iniciales.sql: Usuarios, técnicos, tickets e historiales iniciales de prueba
-- Contraseña para todos los usuarios semilla: 12345
-- Hash BCrypt: $2a$10$pN15HnI.NO6iyFvWeD7vGuPyiQtw9tW1g8ipouR1jhSHlID0wjUQm

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

-- 4. Tickets de Soporte Iniciales
INSERT INTO tickets (requester_id, assignee_id, title, description, category, priority, location, affected_asset, status, created_at, sla_deadline)
VALUES (
    (SELECT id FROM users WHERE email = 'mateo@helpflow.com'),
    (SELECT id FROM users WHERE email = 'andres@helpflow.com'),
    'Caída intermitente de enlace VPN en Finanzas',
    'El equipo de finanzas presenta microcortes recurrentes al conectar con la pasarela bancaria y el ERP.',
    'RED',
    'ALTA',
    'SEDE_CENTRAL',
    'SW-CORE-P2',
    'EN_PROGRESO',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP + INTERVAL '5 hours'
);

INSERT INTO tickets (requester_id, assignee_id, title, description, category, priority, location, affected_asset, status, created_at, sla_deadline)
VALUES (
    (SELECT id FROM users WHERE email = 'mariana@helpflow.com'),
    (SELECT id FROM users WHERE email = 'valentina@helpflow.com'),
    'Error al generar liquidación de nómina en software contable',
    'El aplicativo contable arroja desbordamiento de memoria al consolidar los reportes quincenales en PDF.',
    'SOFTWARE',
    'MEDIA',
    'SEDE_NORTE',
    'SRV-APPS-02',
    'RESUELTO',
    CURRENT_TIMESTAMP - INTERVAL '18 hours',
    CURRENT_TIMESTAMP + INTERVAL '6 hours'
);

INSERT INTO tickets (requester_id, assignee_id, title, description, category, priority, location, affected_asset, status, created_at, sla_deadline)
VALUES (
    (SELECT id FROM users WHERE email = 'esteban@helpflow.com'),
    (SELECT id FROM users WHERE email = 'santiago@helpflow.com'),
    'Fallo crítico en disco duro de estación de trabajo principal',
    'El equipo no inicia el sistema operativo y el BIOS emite alertas de fallo SMART en el disco principal.',
    'HARDWARE',
    'CRITICA',
    'SEDE_SUR',
    'PC-OPER-15',
    'ASIGNADO',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP + INTERVAL '1 hour'
);

INSERT INTO tickets (requester_id, assignee_id, title, description, category, priority, location, affected_asset, status, created_at, sla_deadline)
VALUES (
    (SELECT id FROM users WHERE email = 'mateo@helpflow.com'),
    (SELECT id FROM users WHERE email = 'diana@helpflow.com'),
    'Solicitud de permisos de solo lectura para carpeta compartida',
    'Se requiere acceso para revisar expedientes contables del ejercicio fiscal anterior en el repositorio central.',
    'ACCESOS',
    'BAJA',
    'SEDE_CENTRAL',
    'NAS-FIN-01',
    'CERRADO',
    CURRENT_TIMESTAMP - INTERVAL '30 hours',
    CURRENT_TIMESTAMP + INTERVAL '18 hours'
);

INSERT INTO tickets (requester_id, assignee_id, title, description, category, priority, location, affected_asset, status, created_at, sla_deadline)
VALUES (
    (SELECT id FROM users WHERE email = 'mariana@helpflow.com'),
    NULL,
    'Pérdida de señal en punto de acceso WiFi de sala de reuniones',
    'El access point no asigna direccionamiento DHCP y los dispositivos invitados no logran enlace.',
    'RED',
    'ALTA',
    'SEDE_NORTE',
    'AP-WIFI-N03',
    'ABIERTO',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    CURRENT_TIMESTAMP + INTERVAL '7 hours 30 minutes'
);

-- 5. Historial de Auditoría Inicial
-- Registros de creación y estado actual para evidenciar trazabilidad
INSERT INTO ticket_histories (ticket_id, modified_by_id, previous_status, new_status, observations, created_at)
VALUES
    -- Ticket 1 (En progreso)
    (
        (SELECT id FROM tickets WHERE title = 'Caída intermitente de enlace VPN en Finanzas'),
        (SELECT id FROM users WHERE email = 'mateo@helpflow.com'),
        NULL, 'ABIERTO', 'Ticket registrado por el usuario solicitante.', CURRENT_TIMESTAMP - INTERVAL '3 hours'
    ),
    (
        (SELECT id FROM tickets WHERE title = 'Caída intermitente de enlace VPN en Finanzas'),
        (SELECT id FROM users WHERE email = 'andres@helpflow.com'),
        'ASIGNADO', 'EN_PROGRESO', 'Iniciando diagnóstico en switch y revisión de logs de VPN.', CURRENT_TIMESTAMP - INTERVAL '2 hours'
    ),

    -- Ticket 2 (Resuelto)
    (
        (SELECT id FROM tickets WHERE title = 'Error al generar liquidación de nómina en software contable'),
        (SELECT id FROM users WHERE email = 'mariana@helpflow.com'),
        NULL, 'ABIERTO', 'Ticket registrado por el área de Gestión Humana.', CURRENT_TIMESTAMP - INTERVAL '18 hours'
    ),
    (
        (SELECT id FROM tickets WHERE title = 'Error al generar liquidación de nómina en software contable'),
        (SELECT id FROM users WHERE email = 'valentina@helpflow.com'),
        'EN_PROGRESO', 'RESUELTO', 'Ajuste de memoria heap aplicado y reporte generado con éxito.', CURRENT_TIMESTAMP - INTERVAL '2 hours'
    ),

    -- Ticket 3 (Asignado)
    (
        (SELECT id FROM tickets WHERE title = 'Fallo crítico en disco duro de estación de trabajo principal'),
        (SELECT id FROM users WHERE email = 'esteban@helpflow.com'),
        NULL, 'ABIERTO', 'Reporte de falla crítica de hardware en estación de trabajo.', CURRENT_TIMESTAMP - INTERVAL '1 hour'
    ),
    (
        (SELECT id FROM tickets WHERE title = 'Fallo crítico en disco duro de estación de trabajo principal'),
        (SELECT id FROM users WHERE email = 'admin@helpflow.com'),
        'ABIERTO', 'ASIGNADO', 'Asignación inmediata a técnico especialista en hardware.', CURRENT_TIMESTAMP - INTERVAL '50 minutes'
    ),

    -- Ticket 4 (Cerrado)
    (
        (SELECT id FROM tickets WHERE title = 'Solicitud de permisos de solo lectura para carpeta compartida'),
        (SELECT id FROM users WHERE email = 'mateo@helpflow.com'),
        NULL, 'ABIERTO', 'Solicitud de permisos de auditoría creada.', CURRENT_TIMESTAMP - INTERVAL '30 hours'
    ),
    (
        (SELECT id FROM tickets WHERE title = 'Solicitud de permisos de solo lectura para carpeta compartida'),
        (SELECT id FROM users WHERE email = 'mateo@helpflow.com'),
        'RESUELTO', 'CERRADO', 'Permisos verificados y caso cerrado a satisfacción del usuario.', CURRENT_TIMESTAMP - INTERVAL '10 hours'
    ),

    -- Ticket 5 (Abierto)
    (
        (SELECT id FROM tickets WHERE title = 'Pérdida de señal en punto de acceso WiFi de sala de reuniones'),
        (SELECT id FROM users WHERE email = 'mariana@helpflow.com'),
        NULL, 'ABIERTO', 'Ticket registrado en espera de revisión técnica.', CURRENT_TIMESTAMP - INTERVAL '30 minutes'
    );
