package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

    Optional<Advisor> findByEmail(String email);

    List<Advisor> findByStatus(AdvisorStatus status);

    @Query("""
        SELECT a FROM Advisor a 
        WHERE a.status = :status 
        ORDER BY a.assignedTicketsCount ASC
        """)
    List<Advisor> findByStatusOrderByAssignedTicketsCountAsc(@Param("status") AdvisorStatus status);

    @Query("""
        SELECT a FROM Advisor a 
        WHERE a.status = 'AVAILABLE' 
        ORDER BY a.assignedTicketsCount ASC 
        LIMIT 1
        """)
    Optional<Advisor> findMostAvailableAdvisor();

    long countByStatus(AdvisorStatus status);

    @Query("""
        SELECT a FROM Advisor a 
        WHERE a.moduleNumber = :moduleNumber
        """)
    Optional<Advisor> findByModuleNumber(@Param("moduleNumber") Integer moduleNumber);
}