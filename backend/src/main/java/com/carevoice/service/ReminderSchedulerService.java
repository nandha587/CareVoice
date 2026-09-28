package com.carevoice.service;

import com.carevoice.entity.*;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.repository.MedicationScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final DoseEventRepository doseEventRepository;
    private final MedicationScheduleRepository scheduleRepository;
    private final TelephonyService telephonyService;

    public ReminderSchedulerService(DoseEventRepository doseEventRepository,
                                  MedicationScheduleRepository scheduleRepository,
                                  TelephonyService telephonyService) {
        this.doseEventRepository = doseEventRepository;
        this.scheduleRepository = scheduleRepository;
        this.telephonyService = telephonyService;
    }

    /**
     * Heartbeat scheduler: checks for due doses every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processDueDoses() {
        LocalDateTime now = LocalDateTime.now();
        List<DoseEvent> dueEvents = doseEventRepository.findDueEvents(DoseEventStatus.SCHEDULED, now);

        if (!dueEvents.isEmpty()) {
            log.info("[SCHEDULER] Found {} due medication reminder(s) at {}", dueEvents.size(), now);
        }

        for (DoseEvent event : dueEvents) {
            try {
                // Transition to CALLING
                event.setStatus(DoseEventStatus.CALLING);
                event.setLastCalledAt(LocalDateTime.now());
                event.setCallAttempts(event.getCallAttempts() + 1);
                doseEventRepository.save(event);

                // Place the call via telephony service
                String callSid = telephonyService.initiateCall(event);
                log.info("[SCHEDULER] Initiated outbound call for dose {} | Call SID: {}", event.getId(), callSid);
            } catch (Exception e) {
                log.error("[SCHEDULER] Failed to process due dose {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Nightly lookahead job: generates next day's dose events from active schedules.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void seedDailyDoseEvents() {
        log.info("[SCHEDULER] Running daily lookahead dose seeder...");
        List<MedicationSchedule> activeSchedules = scheduleRepository.findAllActiveSchedules();
        LocalDate today = LocalDate.now();

        for (MedicationSchedule schedule : activeSchedules) {
            try {
                LocalDateTime targetTime = LocalDateTime.of(today, schedule.getReminderTime());
                if (!doseEventRepository.existsByMedicationScheduleIdAndScheduledAt(schedule.getId(), targetTime)) {
                    DoseEvent event = new DoseEvent(schedule, schedule.getMedication().getPatient(), targetTime);
                    doseEventRepository.save(event);
                }
            } catch (Exception e) {
                log.error("[SCHEDULER] Error seeding dose for schedule {}: {}", schedule.getId(), e.getMessage());
            }
        }
    }

    /**
     * Manual trigger for on-demand processing (e.g. from developer testing or demo UI)
     */
    public int triggerDueChecksNow() {
        processDueDoses();
        return doseEventRepository.findDueEvents(DoseEventStatus.SCHEDULED, LocalDateTime.now()).size();
    }
}
