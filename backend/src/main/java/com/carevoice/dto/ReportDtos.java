package com.carevoice.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportDtos {

    public static class DashboardSummaryResponse {
        private long todayTotal;
        private long todayTaken;
        private long todayMissed;
        private long todayNoResponse;
        private long todayScheduled;
        private double weeklyAdherencePercentage;
        private List<MedicationDtos.MedicationResponse> lowStockMedications = new ArrayList<>();
        private List<DoseEventDtos.DoseEventResponse> todayDoses = new ArrayList<>();
        private List<NotificationDtos.NotificationResponse> recentAlerts = new ArrayList<>();

        public DashboardSummaryResponse() {}

        public long getTodayTotal() { return todayTotal; }
        public void setTodayTotal(long todayTotal) { this.todayTotal = todayTotal; }
        public long getTodayTaken() { return todayTaken; }
        public void setTodayTaken(long todayTaken) { this.todayTaken = todayTaken; }
        public long getTodayMissed() { return todayMissed; }
        public void setTodayMissed(long todayMissed) { this.todayMissed = todayMissed; }
        public long getTodayNoResponse() { return todayNoResponse; }
        public void setTodayNoResponse(long todayNoResponse) { this.todayNoResponse = todayNoResponse; }
        public long getTodayScheduled() { return todayScheduled; }
        public void setTodayScheduled(long todayScheduled) { this.todayScheduled = todayScheduled; }
        public double getWeeklyAdherencePercentage() { return weeklyAdherencePercentage; }
        public void setWeeklyAdherencePercentage(double weeklyAdherencePercentage) { this.weeklyAdherencePercentage = weeklyAdherencePercentage; }
        public List<MedicationDtos.MedicationResponse> getLowStockMedications() { return lowStockMedications; }
        public void setLowStockMedications(List<MedicationDtos.MedicationResponse> lowStockMedications) { this.lowStockMedications = lowStockMedications; }
        public List<DoseEventDtos.DoseEventResponse> getTodayDoses() { return todayDoses; }
        public void setTodayDoses(List<DoseEventDtos.DoseEventResponse> todayDoses) { this.todayDoses = todayDoses; }
        public List<NotificationDtos.NotificationResponse> getRecentAlerts() { return recentAlerts; }
        public void setRecentAlerts(List<NotificationDtos.NotificationResponse> recentAlerts) { this.recentAlerts = recentAlerts; }
    }

    public static class WeeklyReportResponse {
        private UUID patientId;
        private String patientName;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private long totalScheduledDoses;
        private long dosesTaken;
        private long dosesMissed;
        private long dosesNoResponse;
        private double adherencePercentage;
        private List<String> lowStockWarnings = new ArrayList<>();
        private String summaryNarrative;

        public WeeklyReportResponse() {}

        public UUID getPatientId() { return patientId; }
        public void setPatientId(UUID patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
        public long getTotalScheduledDoses() { return totalScheduledDoses; }
        public void setTotalScheduledDoses(long totalScheduledDoses) { this.totalScheduledDoses = totalScheduledDoses; }
        public long getDosesTaken() { return dosesTaken; }
        public void setDosesTaken(long dosesTaken) { this.dosesTaken = dosesTaken; }
        public long getDosesMissed() { return dosesMissed; }
        public void setDosesMissed(long dosesMissed) { this.dosesMissed = dosesMissed; }
        public long getDosesNoResponse() { return dosesNoResponse; }
        public void setDosesNoResponse(long dosesNoResponse) { this.dosesNoResponse = dosesNoResponse; }
        public double getAdherencePercentage() { return adherencePercentage; }
        public void setAdherencePercentage(double adherencePercentage) { this.adherencePercentage = adherencePercentage; }
        public List<String> getLowStockWarnings() { return lowStockWarnings; }
        public void setLowStockWarnings(List<String> lowStockWarnings) { this.lowStockWarnings = lowStockWarnings; }
        public String getSummaryNarrative() { return summaryNarrative; }
        public void setSummaryNarrative(String summaryNarrative) { this.summaryNarrative = summaryNarrative; }
    }
}
