package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("""
        SELECT m FROM Mensaje m 
        WHERE m.estadoEnvio = :estado 
        AND m.fechaProgramada <= :fecha
        ORDER BY m.fechaProgramada ASC
        """)
    List<Mensaje> findByEstadoEnvioAndFechaProgramadaLessThanEqual(
        @Param("estado") String estado,
        @Param("fecha") LocalDateTime fecha
    );

    List<Mensaje> findByTicketId(Long ticketId);

    @Query("""
        SELECT m FROM Mensaje m 
        WHERE m.estadoEnvio = 'FALLIDO' 
        AND m.intentos < 4
        """)
    List<Mensaje> findFailedMessagesForRetry();

    long countByEstadoEnvio(String estadoEnvio);
}