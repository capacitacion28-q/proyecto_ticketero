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
@DisplayName("🎯 ESCENARIO COMPLETO AUTOMATIZADO - Validación del Manual")
class EscenarioCompletoTest {

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
    @DisplayName("✅ ESCENARIO COMPLETO: Usuario solicita ticket → Espera → Asignación → Atención → Completado")
    void escenarioCompletoExitoso() {
        System.out.println("🚀 INICIANDO ESCENARIO COMPLETO AUTOMATIZADO");
        System.out.println("=" .repeat(60));
        
        // PASO 1: Usuario pide un ticket
        paso1_CrearTicket();
        
        // PASO 2: Verificar que está en espera
        paso2_VerificarTicketEnEspera();
        
        // PASO 3: Ejecutivo se desocupa
        paso3_EjecutivoDisponible();
        
        // PASO 4: Asignar ticket al ejecutivo
        paso4_AsignarTicket();
        
        // PASO 5: Verificar que está siendo atendido
        paso5_VerificarAtendiendo();
        
        // PASO 6: Completar la atención
        paso6_CompletarAtencion();
        
        // PASO 7: Verificar estado final
        paso7_VerificarEstadoFinal();
        
        System.out.println("=" .repeat(60));
        System.out.println("🎉 ESCENARIO COMPLETO EXITOSO - TODAS LAS VALIDACIONES PASARON");
    }

    private void paso1_CrearTicket() {
        System.out.println("🎫 PASO 1: Usuario pide un ticket");
        
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

        // Validaciones del escenario manual
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketResponse ticket = response.getBody();
        assertThat(ticket).isNotNull();
        
        ticketId = ticket.id();
        ticketNumber = ticket.numero();
        ticketUuid = ticket.codigoReferencia().toString();

        // Validaciones específicas del manual
        assertThat(ticket.status()).isEqualTo(TicketStatus.EN_ESPERA);
        assertThat(ticket.positionInQueue()).isEqualTo(1);
        assertThat(ticket.telefono()).isEqualTo("+56987654321");
        assertThat(ticketNumber).startsWith("C");
        assertThat(ticketUuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

        System.out.println("   ✅ Ticket creado: " + ticketNumber + " (ID: " + ticketId + ")");
        System.out.println("   ✅ UUID: " + ticketUuid);
        System.out.println("   ✅ Teléfono normalizado: " + ticket.telefono());
        System.out.println("   ✅ Estado: " + ticket.status());
        System.out.println("   ✅ Posición en cola: " + ticket.positionInQueue());
    }

    private void paso2_VerificarTicketEnEspera() {
        System.out.println("⏳ PASO 2: Verificar que está en espera");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketUuid, QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.numero()).isEqualTo(ticketNumber);
        assertThat(position.queueType()).isEqualTo(QueueType.CAJA);
        assertThat(position.positionInQueue()).isEqualTo(1);
        
        System.out.println("   ✅ Ticket verificado en cola: " + position.numero());
        System.out.println("   ✅ Tipo de cola: " + position.queueType());
        System.out.println("   ✅ Estado actual: " + position.status());
    }

    private void paso3_EjecutivoDisponible() {
        System.out.println("👨💼 PASO 3: Ejecutivo se desocupa");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"status\": \"AVAILABLE\"}", headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/advisors/1/status", HttpMethod.PUT, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verificar en BD
        Advisor advisor = advisorRepository.findById(1L).orElseThrow();
        assertThat(advisor.getStatus()).isEqualTo(AdvisorStatus.AVAILABLE);
        
        System.out.println("   ✅ Asesor 1 ahora disponible");
        System.out.println("   ✅ Estado del asesor: " + advisor.getStatus());
    }

    private void paso4_AsignarTicket() {
        System.out.println("🔗 PASO 4: Asignar ticket al ejecutivo");
        
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/tickets/" + ticketId + "/assign/1", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("   ✅ Ticket " + ticketNumber + " asignado al asesor 1");
    }

    private void paso5_VerificarAtendiendo() {
        System.out.println("🏃♂️ PASO 5: Verificar que está siendo atendido");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketNumber + "/position", QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.numero()).isEqualTo(ticketNumber);
        assertThat(position.queueType()).isEqualTo(QueueType.CAJA);
        assertThat(position.status()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(position.assignedModuleNumber()).isNotNull();
        assertThat(position.assignedModuleNumber()).isEqualTo(1);
        
        System.out.println("   ✅ Ticket siendo atendido: " + position.numero());
        System.out.println("   ✅ Estado: " + position.status());
        System.out.println("   ✅ Módulo asignado: " + position.assignedModuleNumber());
    }

    private void paso6_CompletarAtencion() {
        System.out.println("✅ PASO 6: Completar la atención");
        
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/tickets/" + ticketId + "/status?status=COMPLETADO", 
                HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("   ✅ Atención completada para ticket " + ticketNumber);
    }

    private void paso7_VerificarEstadoFinal() {
        System.out.println("🔍 PASO 7: Verificar estado final del ticket");
        
        ResponseEntity<QueuePositionResponse> response = restTemplate.getForEntity(
                "/api/tickets/" + ticketNumber + "/position", QueuePositionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QueuePositionResponse position = response.getBody();
        assertThat(position).isNotNull();
        assertThat(position.numero()).isEqualTo(ticketNumber);
        assertThat(position.queueType()).isEqualTo(QueueType.CAJA);
        assertThat(position.status()).isEqualTo(TicketStatus.COMPLETADO);
        assertThat(position.assignedModuleNumber()).isEqualTo(1);
        
        System.out.println("   ✅ Estado final verificado: " + position.status());
        System.out.println("   ✅ Ticket: " + position.numero());
        System.out.println("   ✅ Módulo: " + position.assignedModuleNumber());
        System.out.println("   ✅ Cola: " + position.queueType());
    }

    @Test
    @DisplayName("📱 Validación de normalización de teléfono")
    void validarNormalizacionTelefono() {
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
        
        System.out.println("   ✅ Teléfono normalizado correctamente: " + ticket.telefono());
    }

    @Test
    @DisplayName("🔑 Validación de formato UUID")
    void validarFormatoUuid() {
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
        
        System.out.println("   ✅ UUID válido: " + uuid);
    }
}