package com.carevoice.repository;

import com.carevoice.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findAllByPatientId(UUID patientId);

    @Query("SELECT m FROM Medication m WHERE m.patient.caregiver.id = :caregiverId")
    List<Medication> findAllByCaregiverId(@Param("caregiverId") UUID caregiverId);

    @Query("SELECT m FROM Medication m WHERE m.patient.caregiver.id = :caregiverId AND m.quantityRemaining <= m.lowStockThreshold AND m.active = true")
    List<Medication> findLowStockByCaregiverId(@Param("caregiverId") UUID caregiverId);

    @Query("SELECT m FROM Medication m WHERE m.id = :id AND m.patient.caregiver.id = :caregiverId")
    Optional<Medication> findByIdAndCaregiverId(@Param("id") UUID id, @Param("caregiverId") UUID caregiverId);
}
