package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoReferencia(UUID codigoReferencia);

    Optional<Ticket> findByNumero(String numero);

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.nationalId = :nationalId 
        AND t.status IN :statuses
        """)
    Optional<Ticket> findByNationalIdAndStatusIn(
        @Param("nationalId") String nationalId, 
        @Param("statuses") List<TicketStatus> statuses
    );

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.status = :status 
        ORDER BY t.createdAt ASC
        """)
    List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.queueType = :queueType 
        AND t.status = :status 
        ORDER BY t.createdAt ASC
        """)
    List<Ticket> findByQueueTypeAndStatusOrderByCreatedAtAsc(
        @Param("queueType") QueueType queueType,
        @Param("status") TicketStatus status
    );

    long countByQueueTypeAndStatus(QueueType queueType, TicketStatus status);

    long countByStatus(TicketStatus status);

    @Query("""
        SELECT COUNT(t) FROM Ticket t 
        WHERE t.createdAt >= CURRENT_DATE
        """)
    long countTicketsToday();
}