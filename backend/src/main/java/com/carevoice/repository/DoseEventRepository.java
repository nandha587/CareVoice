package com.carevoice.repository;

import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.DoseEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoseEventRepository extends JpaRepository<DoseEvent, UUID> {

    @Query("SELECT d FROM DoseEvent d " +
           "JOIN FETCH d.patient p " +
           "JOIN FETCH d.medicationSchedule ms " +
           "JOIN FETCH ms.medication m " +
           "WHERE d.status = :status AND d.scheduledAt <= :dueBefore")
    List<DoseEvent> findDueEvents(@Param("status") DoseEventStatus status, @Param("dueBefore") LocalDateTime dueBefore);

    @Query("SELECT d FROM DoseEvent d " +
           "JOIN FETCH d.patient p " +
           "JOIN FETCH d.medicationSchedule ms " +
           "JOIN FETCH ms.medication m " +
           "WHERE p.caregiver.id = :caregiverId AND d.scheduledAt >= :startDate AND d.scheduledAt <= :endDate " +
           "ORDER BY d.scheduledAt DESC")
    List<DoseEvent> findAllByCaregiverIdAndDateRange(
            @Param("caregiverId") UUID caregiverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM DoseEvent d " +
           "JOIN FETCH d.patient p " +
           "JOIN FETCH d.medicationSchedule ms " +
           "JOIN FETCH ms.medication m " +
           "WHERE p.id = :patientId AND d.scheduledAt >= :startDate AND d.scheduledAt <= :endDate " +
           "ORDER BY d.scheduledAt DESC")
    List<DoseEvent> findAllByPatientIdAndDateRange(
            @Param("patientId") UUID patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM DoseEvent d " +
           "JOIN FETCH d.patient p " +
           "JOIN FETCH d.medicationSchedule ms " +
           "JOIN FETCH ms.medication m " +
           "WHERE d.id = :id AND p.caregiver.id = :caregiverId")
    Optional<DoseEvent> findByIdAndCaregiverId(@Param("id") UUID id, @Param("caregiverId") UUID caregiverId);

    @Query("SELECT COUNT(d) FROM DoseEvent d " +
           "WHERE d.patient.caregiver.id = :caregiverId " +
           "AND d.status = :status " +
           "AND d.scheduledAt >= :startDate AND d.scheduledAt <= :endDate")
    long countByCaregiverIdAndStatusAndDateRange(
            @Param("caregiverId") UUID caregiverId,
            @Param("status") DoseEventStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM DoseEvent d " +
           "WHERE d.patient.caregiver.id = :caregiverId " +
           "AND d.scheduledAt >= :startDate AND d.scheduledAt <= :endDate")
    long countTotalByCaregiverIdAndDateRange(
            @Param("caregiverId") UUID caregiverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    boolean existsByMedicationScheduleIdAndScheduledAt(UUID scheduleId, LocalDateTime scheduledAt);
}
