package com.carevoice.repository;

import com.carevoice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.patient p " +
           "LEFT JOIN FETCH n.doseEvent d " +
           "WHERE n.caregiver.id = :caregiverId " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findAllByCaregiverIdOrderByCreatedAtDesc(@Param("caregiverId") UUID caregiverId);
}
