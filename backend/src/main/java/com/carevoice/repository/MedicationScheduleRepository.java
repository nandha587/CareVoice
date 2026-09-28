package com.carevoice.repository;

import com.carevoice.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, UUID> {
    List<MedicationSchedule> findAllByMedicationId(UUID medicationId);

    @Query("SELECT s FROM MedicationSchedule s WHERE s.active = true AND s.medication.active = true")
    List<MedicationSchedule> findAllActiveSchedules();
}
