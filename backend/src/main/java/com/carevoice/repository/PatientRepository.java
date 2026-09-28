package com.carevoice.repository;

import com.carevoice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findAllByCaregiverId(UUID caregiverId);
    Optional<Patient> findByIdAndCaregiverId(UUID id, UUID caregiverId);
}
