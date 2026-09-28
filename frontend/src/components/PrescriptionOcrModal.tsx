import React, { useState } from 'react';
import { Patient, PrescriptionDraft } from '../types';
import { api } from '../api/client';
import { FileText, ShieldAlert, CheckCircle, Upload, X } from 'lucide-react';

interface PrescriptionOcrModalProps {
  patients: Patient[];
  isOpen: boolean;
  onClose: () => void;
  onMedicationCreated: () => void;
}

export const PrescriptionOcrModal: React.FC<PrescriptionOcrModalProps> = ({
  patients,
  isOpen,
  onClose,
  onMedicationCreated,
}) => {
  const [selectedPatientId, setSelectedPatientId] = useState<string>(patients[0]?.id || '');
  const [sampleText, setSampleText] = useState<string>(
    "Rx Prescription - Dr. Michael Harrison, MD\n" +
    "Patient: Eleanor Vance\n" +
    "Rx: Lisinopril 10mg\n" +
    "Sig: Take 1 tablet by mouth daily after breakfast\n" +
    "Dispense: #30 tablets\n" +
    "Refills: 3"
  );
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [draft, setDraft] = useState<PrescriptionDraft | null>(null);

  // Editable Form fields for Caregiver Verification Gate
  const [confirmedName, setConfirmedName] = useState<string>('');
  const [confirmedDosage, setConfirmedDosage] = useState<string>('');
  const [confirmedInstructions, setConfirmedInstructions] = useState<string>('');
  const [confirmedQuantity, setConfirmedQuantity] = useState<number>(30);
  const [lowStockThreshold, setLowStockThreshold] = useState<number>(5);
  const [reminderTime, setReminderTime] = useState<string>('09:00');
  const [frequency, setFrequency] = useState<string>('DAILY');

  if (!isOpen) return null;

  const handleExtractOcr = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPatientId) {
      alert('Please select a patient');
      return;
    }

    setLoading(true);
    try {
      const res = await api.uploadPrescription(selectedPatientId, uploadedFile || undefined, sampleText);
      setDraft(res);
      setConfirmedName(res.parsedMedicineName || '');
      setConfirmedDosage(res.parsedDosage || '');
      setConfirmedInstructions(res.parsedInstructions || '');
      setConfirmedQuantity(res.parsedQuantity || 30);
    } catch (err: any) {
      alert(err.message || 'OCR parsing failed');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmAndActivate = async () => {
    if (!draft) return;
    if (!confirmedName || !confirmedDosage || !confirmedInstructions) {
      alert('Please fill in all confirmed medication details.');
      return;
    }

    setLoading(true);
    try {
      await api.confirmPrescriptionDraft({
        draftId: draft.id,
        confirmedName,
        confirmedDosage,
        confirmedInstructions,
        confirmedQuantity,
        lowStockThreshold,
        reminderTime,
        frequency,
        daysOfWeek: 'ALL',
      });
      alert('Medication verified and activated! Reminder schedule is now active.');
      onMedicationCreated();
      onClose();
    } catch (err: any) {
      alert(err.message || 'Failed to activate medication');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl shadow-2xl max-w-2xl w-full border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center space-x-2">
            <div className="w-9 h-9 rounded-xl bg-green-100 text-green-700 flex items-center justify-center">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900">Prescription OCR Import</h3>
              <p className="text-xs text-slate-500">Scan prescription & verify details before activating</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6 max-h-[80vh] overflow-y-auto">
          {/* Step 1: Upload or Paste */}
          {!draft && (
            <form onSubmit={handleExtractOcr} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-slate-500 mb-1">
                  Target Elderly Patient
                </label>
                <select
                  value={selectedPatientId}
                  onChange={(e) => setSelectedPatientId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  required
                >
                  <option value="">Select patient...</option>
                  {patients.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.fullName} ({p.preferredLanguage})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate-500 mb-1">
                  Upload Prescription Scan / Photo
                </label>
                <div className="border-2 border-dashed border-slate-200 rounded-2xl p-4 text-center hover:border-green-400 transition bg-slate-50">
                  <Upload className="w-8 h-8 text-slate-400 mx-auto mb-2" />
                  <input
                    type="file"
                    accept="image/*,.txt,.pdf"
                    onChange={(e) => setUploadedFile(e.target.files?.[0] || null)}
                    className="text-xs text-slate-600 file:mr-3 file:py-1.5 file:px-3 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-green-600 file:text-white hover:file:bg-green-700 cursor-pointer"
                  />
                  <p className="text-[11px] text-slate-400 mt-2">
                    Supports JPG, PNG, or text scans. Or use the test prescription below.
                  </p>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate-500 mb-1">
                  Prescription Text / OCR Input Sample
                </label>
                <textarea
                  rows={4}
                  value={sampleText}
                  onChange={(e) => setSampleText(e.target.value)}
                  className="w-full p-3 font-mono text-xs border border-slate-200 rounded-xl focus:ring-2 focus:ring-green-500 outline-none bg-slate-50"
                  placeholder="Paste raw prescription text here..."
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-green-600 hover:bg-green-500 text-white font-bold rounded-xl transition shadow-md flex items-center justify-center space-x-2"
              >
                {loading ? (
                  <span>Extracting Structured Fields...</span>
                ) : (
                  <>
                    <Upload className="w-4 h-4" />
                    <span>Process Prescription Image</span>
                  </>
                )}
              </button>
            </form>
          )}

          {/* Step 2: STRICT SAFETY VERIFICATION GATE */}
          {draft && (
            <div className="space-y-5 animate-in fade-in">
              {/* Mandatory Medical Safety Alert Banner */}
              <div className="p-4 bg-amber-50 border-2 border-amber-300 rounded-2xl flex items-start space-x-3 text-amber-900">
                <ShieldAlert className="w-6 h-6 text-amber-600 shrink-0 mt-0.5" />
                <div className="text-xs">
                  <p className="font-bold text-sm text-amber-950">
                    AI/OCR extracted information. Please verify before activating.
                  </p>
                  <p className="mt-1 leading-relaxed text-amber-800">
                    This system does not provide medical diagnoses or alter prescribed regimens. As the caregiver, you must explicitly confirm the medicine name, dosage, instructions, and schedule below.
                  </p>
                </div>
              </div>

              {/* Raw OCR Preview */}
              <div className="p-3 bg-slate-100 rounded-xl text-xs font-mono text-slate-600 border border-slate-200">
                <span className="font-bold text-[10px] text-slate-400 uppercase tracking-wider block mb-1">Extracted Source Text:</span>
                <p className="whitespace-pre-wrap">{draft.rawOcrText}</p>
              </div>

              {/* Verification Form */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Confirmed Medicine Name *
                  </label>
                  <input
                    type="text"
                    value={confirmedName}
                    onChange={(e) => setConfirmedName(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm font-semibold text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Confirmed Dosage *
                  </label>
                  <input
                    type="text"
                    value={confirmedDosage}
                    onChange={(e) => setConfirmedDosage(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    placeholder="e.g. 10mg (1 tablet)"
                    required
                  />
                </div>

                <div className="md:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Caregiver Confirmed Instructions *
                  </label>
                  <textarea
                    rows={2}
                    value={confirmedInstructions}
                    onChange={(e) => setConfirmedInstructions(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    placeholder="e.g. Take with a full glass of water after breakfast"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Quantity Remaining (Pills/Doses) *
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={confirmedQuantity}
                    onChange={(e) => setConfirmedQuantity(parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Low Stock Alert Threshold
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={lowStockThreshold}
                    onChange={(e) => setLowStockThreshold(parseInt(e.target.value) || 5)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Scheduled Call Reminder Time *
                  </label>
                  <input
                    type="time"
                    value={reminderTime}
                    onChange={(e) => setReminderTime(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Reminder Frequency
                  </label>
                  <select
                    value={frequency}
                    onChange={(e) => setFrequency(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm text-slate-900 focus:ring-2 focus:ring-green-500 outline-none"
                  >
                    <option value="DAILY">Daily</option>
                    <option value="TWICE_DAILY">Twice Daily</option>
                    <option value="WEEKDAYS">Weekdays Only</option>
                  </select>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-between pt-4 border-t border-slate-200 space-x-3">
                <button
                  type="button"
                  onClick={() => setDraft(null)}
                  className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:text-slate-800 bg-slate-100 rounded-xl"
                >
                  Back to Upload
                </button>

                <button
                  type="button"
                  onClick={handleConfirmAndActivate}
                  disabled={loading}
                  className="px-6 py-2.5 bg-green-600 hover:bg-green-500 text-white text-sm font-bold rounded-xl shadow-lg shadow-green-900/20 flex items-center space-x-2 transition active:scale-98"
                >
                  <CheckCircle className="w-4 h-4" />
                  <span>Confirm Medication &amp; Activate Reminders</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
