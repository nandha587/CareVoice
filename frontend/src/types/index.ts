export type DoseStatus = 'SCHEDULED' | 'CALLING' | 'TAKEN' | 'MISSED' | 'NO_RESPONSE' | 'CANCELLED';
export type ResponseSource = 'DTMF_PHONE' | 'SIMULATOR' | 'CAREGIVER_MANUAL';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: string;
  email: string;
  fullName: string;
  role: string;
}

export interface Patient {
  id: string;
  fullName: string;
  phoneNumber: string;
  preferredLanguage: string;
  timezone: string;
  emergencyContact?: string;
  notes?: string;
  createdAt: string;
}

export interface Schedule {
  id?: string;
  reminderTime: string; // HH:mm
  frequency: string;
  daysOfWeek: string;
  timezone: string;
  active?: boolean;
}

export interface Medication {
  id: string;
  patientId: string;
  patientName: string;
  name: string;
  dosage: string;
  confirmedInstruction: string;
  quantityRemaining: number;
  lowStockThreshold: number;
  active: boolean;
  isLowStock: boolean;
  schedules: Schedule[];
  createdAt: string;
}

export interface DoseEvent {
  id: string;
  patientId: string;
  patientName: string;
  patientPhone: string;
  preferredLanguage: string;
  medicationId?: string;
  medicationName?: string;
  dosage?: string;
  confirmedInstruction?: string;
  scheduledAt: string;
  status: DoseStatus;
  callAttempts: number;
  lastCalledAt?: string;
  respondedAt?: string;
  responseSource?: ResponseSource;
  notes?: string;
  createdAt: string;
}

export interface NotificationAlert {
  id: string;
  patientId?: string;
  patientName: string;
  doseEventId?: string;
  type: 'MISSED_DOSE' | 'NO_RESPONSE' | 'LOW_STOCK_WARNING' | 'REFILL_REMINDER';
  channel: 'EMAIL' | 'SMS' | 'WHATSAPP' | 'CONSOLE';
  status: 'PENDING' | 'SENT' | 'FAILED';
  message: string;
  sentAt?: string;
  createdAt: string;
}

export interface PrescriptionDraft {
  id: string;
  patientId: string;
  patientName: string;
  originalFilename: string;
  rawOcrText: string;
  parsedMedicineName?: string;
  parsedDosage?: string;
  parsedInstructions?: string;
  parsedFrequency?: string;
  parsedQuantity?: number;
  status: 'PENDING_VERIFICATION' | 'CONFIRMED' | 'REJECTED';
  safetyNotice: string;
  createdAt: string;
}

export interface DashboardSummary {
  todayTotal: number;
  todayTaken: number;
  todayMissed: number;
  todayNoResponse: number;
  todayScheduled: number;
  weeklyAdherencePercentage: number;
  lowStockMedications: Medication[];
  todayDoses: DoseEvent[];
  recentAlerts: NotificationAlert[];
}

export interface WeeklyReport {
  patientId: string;
  patientName: string;
  periodStart: string;
  periodEnd: string;
  totalScheduledDoses: number;
  dosesTaken: number;
  dosesMissed: number;
  dosesNoResponse: number;
  adherencePercentage: number;
  lowStockWarnings: string[];
  summaryNarrative: string;
}

export interface SimulatedCallResult {
  status: string;
  spokenScript: string;
  patientResponse: string;
  updatedEvent: DoseEvent;
}
