# HelpFlow - Sistema Integral de Gestión de Soporte TI

Solución empresarial para la gestión, asignación, escalamiento y cierre de tickets de soporte técnico distribuido en múltiples sedes, desarrollada bajo arquitectura hexagonal, microservicios, seguridad stateless con JWT, observabilidad con Actuator/Micrometer y contenedorización con Docker.

---

## 1. Arquitectura del sistema y relación entre microservicios

El ecosistema se compone de dos microservicios y una base de datos relacional PostgreSQL:

1. **`support-ticket-service` (Puerto 8080)**:
   - Microservicio principal implementado con **Arquitectura Hexagonal**.
   - Núcleo de dominio desacoplado de dependencias externas.
   - Seguridad stateless basada en tokens JWT con roles (`ROLE_USUARIO`, `ROLE_TECNICO`, `ROLE_ADMIN`).
   - Persistencia avanzada con Spring Data JPA, Hibernate, `@NamedEntityGraph` (sin problema N+1) y migraciones automáticas con Flyway.
   - Observabilidad completa con endpoints de Spring Boot Actuator y métricas Prometheus vía Micrometer.
   - Documentación OpenAPI 3 / Swagger UI interactiva.

2. **`technician-dispatch-service` (Puerto 8081)**:
   - Microservicio liviano simulado para despacho técnico.
   - Genera respuestas deterministas mediante seed numérico a partir del identificador de ticket, categoría y prioridad.

### Diagrama de Relación entre Microservicios:
![Diagrama de Microservicios](docs/img/Microservicios.png)

---

## 2. Diagrama de Arquitectura Hexagonal (`support-ticket-service`)

El servicio principal aísla por completo la lógica de negocio y las entidades del framework y de los adaptadores de entrada/salida:

### Diagrama visual de arquitectura hexagonal:
![Diagrama de Arquitectura Hexagonal](docs/img/Arquitectura_Hexagonal.png)

### Estructura física de paquetes:
```
src/main/java/com/helpflow/support_ticket_service/
├── domain                                   <-- Dominio Puro (POJOs y Records Java 21)
│   ├── model                                <-- Entidades, Value Objects y Enums
│   │   ├── User.java, Ticket.java, TicketHistory.java
│   │   └── Role, TicketStatus, TicketPriority, TicketCategory...
│   └── exception                            <-- Excepciones de negocio del dominio
│       ├── DomainException.java
│       ├── UserInactiveException.java
│       ├── InvalidStateTransitionException.java
│       └── UnauthorizedTicketAccessException.java
├── application                              <-- Casos de Uso y Puertos
│   ├── port/in                              <-- Puertos de Entrada
│   │   ├── RegisterUserPort.java, AuthenticateUserPort.java...
│   ├── port/out                             <-- Puertos de Salida (SPI)
│   │   ├── UserRepositoryPort.java, TicketRepositoryPort.java...
│   └── service                              <-- Implementación de los servicios
│       ├── AuthService.java, CreateTicketService.java...
└── infrastructure                           <-- Adaptadores de Infraestructura
    ├── adapter
    │   ├── rest                             <-- Adaptador Web REST y RFC 7807
    │   ├── persistence                      <-- Adaptador JPA + Flyway
    │   ├── client                           <-- Adaptador RestClient para technician-dispatch
    │   ├── security                         <-- Adaptador de Seguridad JWT
    │   └── metrics                          <-- Adaptador Micrometer / Actuator
    └── config
        └── OpenApiConfig.java               <-- Configuración de Swagger
```

---

## 3. Modelo Entidad-Relación (DER)

![Diagrama DER HelpFlow](docs/img/DER-HelpFlow.png)

---

## 4. Diagrama del ciclo de vida del ticket y reglas de negocio

![Ciclo de Vida del Ticket](docs/img/Ciclo_Vida_Ticket.png)

### Reglas de Asignación y SLA:
- **Prioridades y SLA**:
  - `CRITICA`: 2 horas
  - `ALTA`: 8 horas
  - `MEDIA`: 24 horas
  - `BAJA`: 48 horas
- **Criterios de Asignación**:
  - Coincidencia de especialidad del técnico (`department`) con la categoría del ticket (`category`).
  - Coincidencia de sede (`location`).
  - Límite máximo de carga activa por técnico (máximo 5 tickets en curso).
  - Consulta inicial a `technician-dispatch-service`.
- **Escalamiento preventivo**:
  - Tickets activos a menos de 2 horas del vencimiento de SLA son escalados automáticamente a prioridad `CRITICA`.

---

## 5. Usuarios preconfigurados

Todas las cuentas iniciales poseen la contraseña: `12345`

| Rol | Nombre | Correo | Sede | Departamento / Especialidad |
| :--- | :--- | :--- | :--- | :--- |
| **ROLE_ADMIN** | Juan Morales | `admin@helpflow.com` | SEDE_CENTRAL | INFRAESTRUCTURA |
| **ROLE_TECNICO** | Andres Restrepo | `andres@helpflow.com` | SEDE_NORTE | RED |
| **ROLE_TECNICO** | Santiago Henao | `santiago@helpflow.com` | SEDE_CENTRAL | HARDWARE |
| **ROLE_TECNICO** | Valentina Osorio | `valentina@helpflow.com` | SEDE_SUR | SOFTWARE |
| **ROLE_TECNICO** | Diana Mejia | `diana@helpflow.com` | SEDE_CENTRAL | ACCESOS |
| **ROLE_USUARIO** | Mateo Ramirez | `mateo@helpflow.com` | SEDE_CENTRAL | FINANZAS |
| **ROLE_USUARIO** | Mariana Zapata | `mariana@helpflow.com` | SEDE_NORTE | GESTION_HUMANA |
| **ROLE_USUARIO** | Esteban Correa | `esteban@helpflow.com` | SEDE_SUR | OPERACIONES |
| **ROLE_USUARIO** (Inactivo) | Carolina Agudelo | `carolina@helpflow.com` | SEDE_CENTRAL | VENTAS |

---

## 6. Endpoints Principales

### Autenticación (`/auth`)
- `POST /auth/register`: Registro de nuevos usuarios o técnicos con validación corporativa (`@helpflow.com`).
- `POST /auth/login`: Autenticación y obtención de token JWT.

### Gestión de Tickets (`/tickets`) - Requiere `Authorization: Bearer <TOKEN>`
- `POST /tickets`: Creación de ticket con asignación y cálculo de SLA automáticos.
- `GET /tickets`: Listar tickets permitidos según el rol del usuario autenticado (Usuario: sus propios tickets; Técnico: sus asignados; Admin: todos).
- `GET /tickets/{id}`: Detalle de ticket.
- `PATCH /tickets/{id}/status`: Transición de estado (`targetStatus` y `observations`).
- `PUT /tickets/{id}/reassign`: Reasignación de técnico (solo `ROLE_ADMIN`).
- `GET /tickets/{id}/history`: Auditoría del historial de cambios de estado.
- `POST /tickets/escalate`: Escalamiento de tickets próximos a vencer SLA (solo `ROLE_ADMIN`).

### Documentación y Observabilidad
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Métricas**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

---

## 7. Evidencias de Observabilidad, Métricas y Logs

### A. Métricas Generales en Spring Boot Actuator (`/actuator/metrics`)
Consulta del listado completo de métricas del sistema, incluyendo métricas personalizadas del dominio HelpFlow (`helpflow_*`):

![Métricas generales en actuator](docs/img/Metricas_generales_en_actuator.png)

---

### B. Métrica de tickets creados por categoría y prioridad (`helpflow_tickets_created_total`)
Conteo dinámico de tickets creados con tags de dimensión para categorización y análisis:

![Métricas de tickets creados por categoría y prioridad](docs/img/Metricas_de_tickets_creados_por_categoria_y_prioridad.png)

---

### C. Métrica en Formato Prometheus (`/actuator/prometheus`)
Exposición de métricas listas para scraping en formato estándar Prometheus (OpenMetrics):

![Métrica en formato Prometheus](docs/img/Metrica_en_formato_Prometheus.png)

---

### D. Detección y métrica de tickets vencidos según SLA

1. **Métrica `helpflow_tickets_breached_sla_total` reflejando tickets fuera de tiempo (`value: 1.0`)**:
   ![Ticket vencido según SLA en métrica](docs/img/ticket_vencido_segun_SLA.png)

2. **Detalle del ticket vía API (`GET /tickets/1`) mostrando `"slaBreached": true`**:
   ![Ticket vencido según SLA en endpoint](docs/img/ticket_vencido_segun_SLA_endpoint.png)

---

### E. Logs Estructurados
Registro de trazas con formato estructurado ISO 8601, niveles de severidad (`INFO`, `WARN`), identificación de hilos y captura de excepciones mediante el manejador global RFC 7807:

![Captura de logs estructurados](docs/img/Captura_de_logs.png)

---

## 8. Despliegue con Docker y Docker Compose

### Requisitos:
- Docker y Docker Compose instalados.

### Archivo de Entorno:
Copie el archivo de ejemplo si no existe:
```bash
cp .env.example .env
```

### Iniciar los Servicios:
```bash
docker compose up --build -d
```

### Detener los Servicios:
```bash
docker compose down
```

### Ejecución Local sin Docker:
1. Asegurarse de tener PostgreSQL corriendo en el puerto 5436 con la base de datos `helpflow_db`.
2. Iniciar `technician-dispatch-service`:
   ```bash
   cd technician-dispatch-service
   mvn spring-boot:run
   ```
3. Iniciar `support-ticket-service`:
   ```bash
   cd support-ticket-service
   mvn spring-boot:run
   ```
## AUTOR

**JUAN PABLO CAÑAS**<BR>
**RIWI CODER**<br>
**GITHUB:** https://github.com/JPCanas-dev
