import {
  AuthResponse,
  Patient,
  Medication,
  DoseEvent,
  DoseStatus,
  DashboardSummary,
  WeeklyReport,
  NotificationAlert,
  PrescriptionDraft,
  SimulatedCallResult,
  Schedule
} from '../types';

const API_BASE = '/api/v1';

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('carevoice_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const headers = {
    'Content-Type': 'application/json',
    ...getAuthHeader(),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem('carevoice_token');
    localStorage.removeItem('carevoice_user');
    window.location.href = '/login';
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    let errorMsg = 'An unexpected error occurred';
    try {
      const errorJson = await response.json();
      errorMsg = errorJson.message || errorJson.error || JSON.stringify(errorJson);
    } catch {
      errorMsg = await response.text();
    }
    throw new Error(errorMsg);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const api = {
  // Auth
  login: (data: { email: string; password: string }) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify(data) }),

  register: (data: { fullName: string; email: string; password: string }) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(data) }),

  // Patients
  getPatients: () => request<Patient[]>('/patients'),
  getPatientById: (id: string) => request<Patient>(`/patients/${id}`),
  createPatient: (data: Partial<Patient>) =>
    request<Patient>('/patients', { method: 'POST', body: JSON.stringify(data) }),
  updatePatient: (id: string, data: Partial<Patient>) =>
    request<Patient>(`/patients/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deletePatient: (id: string) =>
    request<void>(`/patients/${id}`, { method: 'DELETE' }),

  // Medications
  getAllMedications: () => request<Medication[]>('/medications'),
  getMedicationsByPatient: (patientId: string) =>
    request<Medication[]>(`/medications/patient/${patientId}`),
  getLowStockMedications: () => request<Medication[]>('/medications/low-stock'),
  createMedication: (data: {
    patientId: string;
    name: string;
    dosage: string;
    confirmedInstruction: string;
    quantityRemaining: number;
    lowStockThreshold: number;
    schedules: Schedule[];
  }) => request<Medication>('/medications', { method: 'POST', body: JSON.stringify(data) }),
  deleteMedication: (id: string) =>
    request<void>(`/medications/${id}`, { method: 'DELETE' }),

  // Doses
  getTodayDoses: () => request<DoseEvent[]>('/doses/today'),
  getDosesByPatient: (patientId: string, date?: string) =>
    request<DoseEvent[]>(`/doses/patient/${patientId}${date ? `?date=${date}` : ''}`),
  updateDoseStatus: (id: string, status: DoseStatus, notes?: string) =>
    request<DoseEvent>(`/doses/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status, notes }),
    }),
  simulateCall: (doseEventId: string, keypadInput: string) =>
    request<SimulatedCallResult>('/doses/simulate-call', {
      method: 'POST',
      body: JSON.stringify({ doseEventId, keypadInput }),
    }),
  triggerScheduler: () =>
    request<{ message: string; remainingDueDoses: number }>('/doses/trigger-scheduler', {
      method: 'POST',
    }),

  // Reports & Dashboard
  getDashboardSummary: () => request<DashboardSummary>('/reports/dashboard'),
  getWeeklyReport: (patientId: string) =>
    request<WeeklyReport>(`/reports/weekly/${patientId}`),
  getNotifications: () => request<NotificationAlert[]>('/reports/notifications'),

  // Prescription OCR
  uploadPrescription: async (patientId: string, file?: File, sampleText?: string): Promise<PrescriptionDraft> => {
    const formData = new FormData();
    formData.append('patientId', patientId);
    if (file) formData.append('file', file);
    if (sampleText) formData.append('sampleText', sampleText);

    const token = localStorage.getItem('carevoice_token');
    const response = await fetch(`${API_BASE}/prescriptions/upload`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: formData,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to upload prescription' }));
      throw new Error(err.message || 'Upload failed');
    }
    return response.json();
  },

  getPrescriptionDrafts: () => request<PrescriptionDraft[]>('/prescriptions/drafts'),

  confirmPrescriptionDraft: (data: {
    draftId: string;
    confirmedName: string;
    confirmedDosage: string;
    confirmedInstructions: string;
    confirmedQuantity: number;
    lowStockThreshold: number;
    reminderTime: string;
    frequency: string;
    daysOfWeek: string;
  }) => request<Medication>('/prescriptions/confirm', { method: 'POST', body: JSON.stringify(data) }),
};
