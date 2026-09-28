package com.carevoice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String confirmedInstruction;

    @Column(nullable = false)
    private Integer quantityRemaining = 30;

    @Column(nullable = false)
    private Integer lowStockThreshold = 5;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicationSchedule> schedules = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public Medication() {}

    public Medication(Patient patient, String name, String dosage, String confirmedInstruction, Integer quantityRemaining, Integer lowStockThreshold) {
        this.patient = patient;
        this.name = name;
        this.dosage = dosage;
        this.confirmedInstruction = confirmedInstruction;
        this.quantityRemaining = quantityRemaining != null ? quantityRemaining : 30;
        this.lowStockThreshold = lowStockThreshold != null ? lowStockThreshold : 5;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getConfirmedInstruction() {
        return confirmedInstruction;
    }

    public void setConfirmedInstruction(String confirmedInstruction) {
        this.confirmedInstruction = confirmedInstruction;
    }

    public Integer getQuantityRemaining() {
        return quantityRemaining;
    }

    public void setQuantityRemaining(Integer quantityRemaining) {
        this.quantityRemaining = quantityRemaining;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<MedicationSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<MedicationSchedule> schedules) {
        this.schedules = schedules;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
