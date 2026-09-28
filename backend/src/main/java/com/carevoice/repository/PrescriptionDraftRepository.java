package com.carevoice.repository;

import com.carevoice.entity.PrescriptionDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionDraftRepository extends JpaRepository<PrescriptionDraft, UUID> {

    @Query("SELECT p FROM PrescriptionDraft p " +
           "JOIN FETCH p.patient " +
           "WHERE p.caregiver.id = :caregiverId " +
           "ORDER BY p.createdAt DESC")
    List<PrescriptionDraft> findAllByCaregiverId(@Param("caregiverId") UUID caregiverId);

    @Query("SELECT p FROM PrescriptionDraft p " +
           "JOIN FETCH p.patient " +
           "WHERE p.id = :id AND p.caregiver.id = :caregiverId")
    Optional<PrescriptionDraft> findByIdAndCaregiverId(@Param("id") UUID id, @Param("caregiverId") UUID caregiverId);
}
