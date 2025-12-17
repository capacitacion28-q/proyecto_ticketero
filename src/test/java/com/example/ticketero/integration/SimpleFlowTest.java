package com.example.ticketero.integration;

import com.example.ticketero.model.dto.*;
import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("🎯 Prueba Automatizada del Escenario Completo Manual")
class SimpleFlowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private AdvisorRepository advisorRepository;

    private Long ticketId;
    private String ticketNumber;
    private String ticketUuid;

    @BeforeEach
    void setUp() {
        // Crear asesor para las pruebas
        Advisor advisor = Advisor.builder()
                .name("Juan Pérez")
                .email("juan.perez@test.com")
                .moduleNumber(1)
                .status(AdvisorStatus.BUSY)
                .build();
        advisorRepository.save(advisor);
    }

    @Test
    @DisplayName("Escenario completo: Crear → Asignar → Completar ticket")
    void testCompleteTicketFlow() {
        System.out.println("🚀 INICIANDO PRUEBA AUTOMATIZADA DEL ESCENARIO COMPLETO");
        
        // PASO 1: Crear ticket
        createTicket();
        
        // PASO 2: Verificar ticket creado
        verifyTicketCreated();
        
        // PASO 3: Ver dashboard inicial
        checkInitialDashboard();
        
        // PASO 4: Hacer asesor disponible
        makeAdvisorAvailable();
        
        // PASO 5: Asignar ticket
        assignTicket();
        
        // PASO 6: Verificar asignación
        verifyTicketAssigned();
        
        // PASO 7: Completar ticket
        completeTicket();
        
        // PASO 8: Verificar completado
        verifyTicketCompleted();
        
        // PASO 9: Dashboard final
        checkFinalDashboard();
        
        System.out.println("🎉 PRUEBA COMPLETADA EXITOSAMENTE");
    }

    private void createTicket() {
        System.out.println("🎫 PASO 1: Creando ticket...");
        
        TicketCreateRequest request = new TicketCreateRequest(
                "12345678",
                "987654321",
                "Sucursal Centro",
                QueueType.CAJA
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TicketCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
                "/api/tickets", entity, TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketResponse ticket = response.getBody();
        assertThat(ticket).isNotNull();

        ticketId = ticket.id();
        ticketNumber = ticket.numero();
        ticketUuid = ticket.codigoReferencia().toString();

        System.out.println("✅ Ticket creado: " + ticketNumber + " (ID: " + ticketId + ")");
        
        // Validaciones del escenario manual
        assertThat(ticket.status()).isEqualTo(TicketStatus.EN_ESPERA);
        assertThat(ticket.positionInQueue()).isEqualTo(1);
        assertThat(ticket.telefono()).isEqualTo("+56987654321");
        assertThat(ticketNumber).startsWith("C");
    }

    private void verifyTicketCreated() {
        System.out.println("⏳ PASO 2: Verificando ticket creado...");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketUuid, QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.numero()).isEqualTo(ticketNumber);
        assertThat(position.queueType()).isEqualTo(QueueType.CAJA);
        
        System.out.println("✅ Ticket verificado en cola");
    }

    private void checkInitialDashboard() {
        System.out.println("📊 PASO 3: Verificando dashboard inicial...");
        
        ResponseEntity<DashboardResponse> response = restTemplate.getForEntity(
                "/api/admin/summary", DashboardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DashboardResponse dashboard = response.getBody();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.summary().totalTicketsToday()).isGreaterThanOrEqualTo(1);
        
        System.out.println("✅ Dashboard inicial: " + dashboard.summary().totalTicketsToday() + " tickets hoy");
    }

    private void makeAdvisorAvailable() {
        System.out.println("👨💼 PASO 4: Haciendo asesor disponible...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"status\": \"AVAILABLE\"}", headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/advisors/1/status", HttpMethod.PUT, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verificar en BD
        Advisor advisor = advisorRepository.findById(1L).orElseThrow();
        assertThat(advisor.getStatus()).isEqualTo(AdvisorStatus.AVAILABLE);
        
        System.out.println("✅ Asesor 1 disponible");
    }

    private void assignTicket() {
        System.out.println("🔗 PASO 5: Asignando ticket al asesor...");
        
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/tickets/" + ticketId + "/assign/1", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("✅ Ticket asignado al asesor 1");
    }

    private void verifyTicketAssigned() {
        System.out.println("🏃♂️ PASO 6: Verificando ticket asignado...");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketNumber + "/position", QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.status()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(position.assignedModuleNumber()).isEqualTo(1);
        
        System.out.println("✅ Ticket siendo atendido en módulo 1");
    }

    private void completeTicket() {
        System.out.println("✅ PASO 7: Completando ticket...");
        
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/tickets/" + ticketId + "/status?status=COMPLETADO", 
                HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("✅ Ticket completado");
    }

    private void verifyTicketCompleted() {
        System.out.println("🔍 PASO 8: Verificando ticket completado...");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketNumber + "/position", QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.status()).isEqualTo(TicketStatus.COMPLETADO);
        assertThat(position.assignedModuleNumber()).isEqualTo(1);
        
        System.out.println("✅ Estado final verificado: COMPLETADO");
    }

    private void checkFinalDashboard() {
        System.out.println("📈 PASO 9: Verificando dashboard final...");
        
        ResponseEntity<DashboardResponse> response = restTemplate.getForEntity(
                "/api/admin/summary", DashboardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DashboardResponse dashboard = response.getBody();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.summary().ticketsCompleted()).isEqualTo(1);
        
        System.out.println("✅ Dashboard final: " + dashboard.summary().ticketsCompleted() + " ticket completado");
        System.out.println("📊 Métricas finales: " + dashboard.summary());
    }

    @Test
    @DisplayName("Validación de formato de teléfono")
    void testPhoneNormalization() {
        System.out.println("📱 Probando normalización de teléfono...");
        
        TicketCreateRequest request = new TicketCreateRequest(
                "87654321", "912345678", "Sucursal Norte", QueueType.GERENCIA);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TicketCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
                "/api/tickets", entity, TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketResponse ticket = response.getBody();
        assertThat(ticket).isNotNull();
        assertThat(ticket.telefono()).isEqualTo("+56912345678");
        
        System.out.println("✅ Teléfono normalizado correctamente: " + ticket.telefono());
    }

    @Test
    @DisplayName("Validación de UUID en código de referencia")
    void testUuidFormat() {
        System.out.println("🔑 Probando formato UUID...");
        
        TicketCreateRequest request = new TicketCreateRequest(
                "11111111", "999888777", "Sucursal Sur", QueueType.CAJA);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TicketCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
                "/api/tickets", entity, TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketResponse ticket = response.getBody();
        assertThat(ticket).isNotNull();
        
        String uuid = ticket.codigoReferencia().toString();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        
        System.out.println("✅ UUID válido: " + uuid);
    }
}