package com.carevoice;

import com.carevoice.dto.MedicationDtos;
import com.carevoice.dto.PatientDtos;
import com.carevoice.entity.DoseEvent;
import com.carevoice.entity.DoseEventStatus;
import com.carevoice.entity.ResponseSource;
import com.carevoice.entity.User;
import com.carevoice.repository.DoseEventRepository;
import com.carevoice.repository.UserRepository;
import com.carevoice.service.MedicationService;
import com.carevoice.service.NotificationService;
import com.carevoice.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class CareVoiceApplication {

    private static final Logger log = LoggerFactory.getLogger(CareVoiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CareVoiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            PatientService patientService,
            MedicationService medicationService,
            DoseEventRepository doseEventRepository,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmail("caregiver@carevoice.com")) {
                log.info("Demo caregiver user already exists.");
                return;
            }

            log.info("Seeding initial CareVoice demo data...");

            // 1. Create demo caregiver
            User caregiver = new User(
                    "Sarah Miller",
                    "caregiver@carevoice.com",
                    passwordEncoder.encode("CareVoice2026!"),
                    "ROLE_CAREGIVER"
            );
            caregiver = userRepository.save(caregiver);
            log.info("Created Demo Caregiver: caregiver@carevoice.com (Password: CareVoice2026!)");

            // 2. Create elderly patient 1: Eleanor Vance
            PatientDtos.PatientRequest p1Req = new PatientDtos.PatientRequest();
            p1Req.setFullName("Eleanor Vance");
            p1Req.setPhoneNumber("+15551234567");
            p1Req.setPreferredLanguage("en-US");
            p1Req.setTimezone("America/New_York");
            p1Req.setEmergencyContact("Sarah Miller (Daughter) - +15559876543");
            p1Req.setNotes("Mild hypertension and joint stiffness. Prefers gentle voice reminders at 9 AM.");
            PatientDtos.PatientResponse p1 = patientService.createPatient(caregiver.getId(), p1Req);

            // 3. Create elderly patient 2: Ramesh Sharma
            PatientDtos.PatientRequest p2Req = new PatientDtos.PatientRequest();
            p2Req.setFullName("Ramesh Sharma");
            p2Req.setPhoneNumber("+919876543210");
            p2Req.setPreferredLanguage("hi-IN");
            p2Req.setTimezone("Asia/Kolkata");
            p2Req.setEmergencyContact("Pooja Sharma (Daughter) - +919876543211");
            p2Req.setNotes("Type 2 diabetes. Voice reminders configured in Hindi.");
            PatientDtos.PatientResponse p2 = patientService.createPatient(caregiver.getId(), p2Req);

            // 4. Create Medication 1 for Eleanor: Lisinopril
            MedicationDtos.MedicationRequest med1 = new MedicationDtos.MedicationRequest();
            med1.setPatientId(p1.getId());
            med1.setName("Lisinopril");
            med1.setDosage("10mg (1 tablet)");
            med1.setConfirmedInstruction("Take after breakfast with a full glass of water");
            med1.setQuantityRemaining(28);
            med1.setLowStockThreshold(7);
            med1.getSchedules().add(new MedicationDtos.ScheduleDto("09:00", "DAILY", "ALL", "America/New_York"));
            MedicationDtos.MedicationResponse m1 = medicationService.createMedication(caregiver.getId(), med1);

            // 5. Create Medication 2 for Eleanor: Atorvastatin (Low stock demo)
            MedicationDtos.MedicationRequest med2 = new MedicationDtos.MedicationRequest();
            med2.setPatientId(p1.getId());
            med2.setName("Atorvastatin");
            med2.setDosage("20mg (1 tablet)");
            med2.setConfirmedInstruction("Take before bedtime");
            med2.setQuantityRemaining(4); // Trigger low stock!
            med2.setLowStockThreshold(7);
            med2.getSchedules().add(new MedicationDtos.ScheduleDto("21:00", "DAILY", "ALL", "America/New_York"));
            MedicationDtos.MedicationResponse m2 = medicationService.createMedication(caregiver.getId(), med2);

            // 6. Create Medication 3 for Ramesh: Metformin
            MedicationDtos.MedicationRequest med3 = new MedicationDtos.MedicationRequest();
            med3.setPatientId(p2.getId());
            med3.setName("Metformin");
            med3.setDosage("500mg (1 tablet)");
            med3.setConfirmedInstruction("Khana khane ke baad lein (Take with meals)");
            med3.setQuantityRemaining(45);
            med3.setLowStockThreshold(10);
            med3.getSchedules().add(new MedicationDtos.ScheduleDto("08:30", "DAILY", "ALL", "Asia/Kolkata"));
            MedicationDtos.MedicationResponse m3 = medicationService.createMedication(caregiver.getId(), med3);

            // 7. Update one dose event to TAKEN and one to MISSED for rich dashboard metrics
            LocalDate today = LocalDate.now();
            List<DoseEvent> todayDoses = doseEventRepository.findAll();
            if (!todayDoses.isEmpty()) {
                DoseEvent first = todayDoses.get(0);
                first.setStatus(DoseEventStatus.TAKEN);
                first.setRespondedAt(LocalDateTime.of(today, LocalTime.of(9, 2)));
                first.setResponseSource(ResponseSource.DTMF_PHONE);
                first.setNotes("Dose confirmed taken by Eleanor Vance via phone keypad 1");
                doseEventRepository.save(first);
            }

            log.info("CareVoice demo data successfully seeded!");
            log.info("Registered Patients: Eleanor Vance (+15551234567, en-US), Ramesh Sharma (+919876543210, hi-IN)");
        };
    }
}
