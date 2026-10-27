package com.helpflow.support_ticket_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort;
import com.helpflow.support_ticket_service.application.port.in.CreateTicketPort;
import com.helpflow.support_ticket_service.application.port.in.GetTicketPort;
import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort;
import com.helpflow.support_ticket_service.application.port.in.RegisterUserPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.domain.model.UserStatus;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.advice.GlobalExceptionHandler;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.controller.AuthController;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.controller.TicketController;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.AuthDto.RegisterRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.CreateTicketRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.UpdateStatusRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.security.SecurityUser;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas de integracion web para controladores REST, manejo de excepciones y DTOs
@ExtendWith(MockitoExtension.class)
class WebMvcIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegisterUserPort registerUserPort;

    @Mock
    private AuthenticateUserPort authenticateUserPort;

    @Mock
    private CreateTicketPort createTicketPort;

    @Mock
    private ManageTicketPort manageTicketPort;

    @Mock
    private GetTicketPort getTicketPort;

    @Mock
    private TicketRepositoryPort ticketRepositoryPort;

    private User sampleUser;
    private SecurityUser securityUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setDocument("1017234890");
        sampleUser.setName("Juan Perez");
        sampleUser.setEmail("juan.perez@helpflow.com");
        sampleUser.setPassword("encoded");
        sampleUser.setRole(Role.ROLE_USUARIO);
        sampleUser.setLocation("SEDE_CENTRAL");
        sampleUser.setDepartment("FINANZAS");
        sampleUser.setStatus(UserStatus.ACTIVO);

        securityUser = new SecurityUser(sampleUser);

        AuthController authController = new AuthController(registerUserPort, authenticateUserPort);
        TicketController ticketController = new TicketController(createTicketPort, manageTicketPort, getTicketPort, () -> 0);

        TicketMetricsAdapter metricsAdapter = new TicketMetricsAdapter(new SimpleMeterRegistry(), ticketRepositoryPort);

        // Resolver para inyectar @AuthenticationPrincipal en pruebas standalone
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return securityUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(authController, ticketController)
                .setControllerAdvice(new GlobalExceptionHandler(metricsAdapter))
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    void registerUser_ShouldReturn201_WhenPayloadIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "1017234890",
                "Juan Perez",
                "juan.perez@helpflow.com",
                "12345",
                Role.ROLE_USUARIO,
                "SEDE_CENTRAL",
                "FINANZAS",
                new BigDecimal("2500000"),
                null
        );

        when(registerUserPort.register(any())).thenReturn(sampleUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan.perez@helpflow.com"))
                .andExpect(jsonPath("$.document").value("1017234890"));
    }

    @Test
    void registerUser_ShouldReturnProblemDetail_WhenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "1017234890",
                "Juan Perez",
                "invalido@gmail.com", // No es @helpflow.com
                "12345",
                Role.ROLE_USUARIO,
                "SEDE_CENTRAL",
                "FINANZAS",
                null,
                null
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.invalidFields.email").exists());
    }

    @Test
    void createTicket_ShouldReturn201_WhenPayloadIsValid() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "Falla en router",
                "El router del area no da señal de red",
                TicketCategory.RED,
                TicketPriority.ALTA,
                "SEDE_CENTRAL",
                "ROUTER-01"
        );

        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setRequesterId(1L);
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setLocation(request.location());
        ticket.setStatus(TicketStatus.ASIGNADO);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setSlaDeadline(LocalDateTime.now().plusHours(8));

        when(createTicketPort.createTicket(any())).thenReturn(ticket);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("ASIGNADO"));
    }

    @Test
    void getTickets_ShouldReturnList_WhenAuthorized() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Falla en router");
        ticket.setStatus(TicketStatus.ABIERTO);
        ticket.setPriority(TicketPriority.MEDIA);
        ticket.setCategory(TicketCategory.RED);

        when(getTicketPort.getTicketsForUser(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Falla en router"));
    }

    @Test
    void updateTicketStatus_ShouldReturnUpdatedTicket() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(
                TicketStatus.EN_PROGRESO,
                "Iniciando labores tecnicas"
        );

        Ticket updatedTicket = new Ticket();
        updatedTicket.setId(10L);
        updatedTicket.setStatus(TicketStatus.EN_PROGRESO);

        when(manageTicketPort.updateStatus(any())).thenReturn(updatedTicket);

        mockMvc.perform(patch("/tickets/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_PROGRESO"));
    }
}
