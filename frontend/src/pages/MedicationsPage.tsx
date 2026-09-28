import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Medication, Patient } from '../types';
import { PrescriptionOcrModal } from '../components/PrescriptionOcrModal';
import { Pill, Plus, Clock, AlertTriangle, FileText, Trash2 } from 'lucide-react';

export const MedicationsPage: React.FC = () => {
  const [medications, setMedications] = useState<Medication[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
  const [isOcrOpen, setIsOcrOpen] = useState<boolean>(false);

  // Form states
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [name, setName] = useState<string>('');
  const [dosage, setDosage] = useState<string>('');
  const [confirmedInstruction, setConfirmedInstruction] = useState<string>('');
  const [quantityRemaining, setQuantityRemaining] = useState<number>(30);
  const [lowStockThreshold, setLowStockThreshold] = useState<number>(5);
  const [reminderTime, setReminderTime] = useState<string>('09:00');
  const [frequency, setFrequency] = useState<string>('DAILY');

  const loadData = async () => {
    try {
      const [meds, pats] = await Promise.all([
        api.getAllMedications(),
        api.getPatients(),
      ]);
      setMedications(meds);
      setPatients(pats);
      if (pats.length > 0 && !selectedPatientId) {
        setSelectedPatientId(pats[0].id);
      }
    } catch (err) {
      console.error('Failed to load medications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateMedication = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPatientId) {
      alert('Please select a patient');
      return;
    }

    try {
      await api.createMedication({
        patientId: selectedPatientId,
        name,
        dosage,
        confirmedInstruction,
        quantityRemaining,
        lowStockThreshold,
        schedules: [
          {
            reminderTime,
            frequency,
            daysOfWeek: 'ALL',
            timezone: 'UTC',
          },
        ],
      });
      setIsAddModalOpen(false);
      resetForm();
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to save medication');
    }
  };

  const handleDeleteMedication = async (id: string, medName: string) => {
    if (!window.confirm(`Deactivate reminder schedule for ${medName}?`)) return;
    try {
      await api.deleteMedication(id);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to delete');
    }
  };

  const resetForm = () => {
    setName('');
    setDosage('');
    setConfirmedInstruction('');
    setQuantityRemaining(30);
    setLowStockThreshold(5);
    setReminderTime('09:00');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center space-x-2">
            <Pill className="w-6 h-6 text-green-600" />
            <span>Medications &amp; Reminder Schedules</span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Configure confirmed instructions, scheduled voice reminder times, and pill inventory counts.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={() => setIsOcrOpen(true)}
            className="px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs rounded-xl shadow flex items-center space-x-1.5 transition"
          >
            <FileText className="w-4 h-4 text-green-400" />
            <span>Import via Prescription OCR</span>
          </button>

          <button
            onClick={() => {
              resetForm();
              setIsAddModalOpen(true);
            }}
            className="px-5 py-2.5 bg-green-600 hover:bg-green-500 text-white font-bold text-xs rounded-xl shadow-lg shadow-green-900/20 flex items-center space-x-1.5 transition"
          >
            <Plus className="w-4 h-4" />
            <span>Add Medication</span>
          </button>
        </div>
      </div>

      {/* Medications List */}
      {loading ? (
        <div className="text-center py-12 text-slate-400">Loading medications...</div>
      ) : medications.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-sm">
          <Pill className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="font-bold text-slate-800 text-base">No active medications scheduled</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            Add a confirmed medication schedule or scan a prescription to start automated phone reminders.
          </p>
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="mt-4 px-4 py-2 bg-green-600 text-white text-xs font-bold rounded-xl"
          >
            Add First Medication
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {medications.map((med) => (
            <div
              key={med.id}
              className={`bg-white rounded-3xl p-6 border shadow-sm hover:shadow-md transition flex flex-col justify-between ${
                med.isLowStock ? 'border-amber-300 bg-amber-50/20' : 'border-slate-200/80'
              }`}
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-green-700 bg-green-50 px-2 py-0.5 rounded-md">
                      {med.patientName}
                    </span>
                    <h3 className="text-xl font-bold text-slate-900 mt-2">{med.name}</h3>
                    <p className="text-xs font-semibold text-slate-500">{med.dosage}</p>
                  </div>
                  <button
                    onClick={() => handleDeleteMedication(med.id, med.name)}
                    className="p-1.5 text-slate-300 hover:text-red-600 rounded-lg transition"
                    title="Deactivate Schedule"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="mt-4 p-3 bg-slate-50 rounded-2xl border border-slate-100 text-xs">
                  <span className="font-bold text-slate-500 uppercase text-[10px] block mb-1">
                    Confirmed Spoken Instruction:
                  </span>
                  <p className="text-slate-800 italic leading-relaxed">
                    "{med.confirmedInstruction}"
                  </p>
                </div>

                {/* Stock Level Bar */}
                <div className="mt-4 space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-slate-500">Remaining Inventory:</span>
                    <span className={`font-bold ${med.isLowStock ? 'text-red-600' : 'text-slate-800'}`}>
                      {med.quantityRemaining} doses left
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div
                      className={`h-full transition-all duration-500 ${
                        med.isLowStock ? 'bg-red-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${Math.min(100, (med.quantityRemaining / 30) * 100)}%` }}
                    ></div>
                  </div>
                  {med.isLowStock && (
                    <div className="flex items-center space-x-1 text-red-600 text-[11px] font-semibold mt-1">
                      <AlertTriangle className="w-3.5 h-3.5" />
                      <span>Low stock warning (Threshold: {med.lowStockThreshold})</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Scheduled Times */}
              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                <div className="flex items-center space-x-1.5 text-xs text-slate-600">
                  <Clock className="w-4 h-4 text-slate-400" />
                  <span>
                    Call Time:{' '}
                    <strong className="text-slate-900 font-mono">
                      {med.schedules?.[0]?.reminderTime || '09:00'}
                    </strong>
                  </span>
                </div>
                <span className="text-[11px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                  {med.schedules?.[0]?.frequency || 'DAILY'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Medication Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <h3 className="text-lg font-bold text-slate-900">Add Medication &amp; Voice Schedule</h3>
              <button
                onClick={() => setIsAddModalOpen(false)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateMedication} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Elderly Patient *
                </label>
                <select
                  value={selectedPatientId}
                  onChange={(e) => setSelectedPatientId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
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
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Medicine Name *
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none font-semibold text-slate-900"
                  placeholder="e.g. Lisinopril"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Dosage *
                </label>
                <input
                  type="text"
                  value={dosage}
                  onChange={(e) => setDosage(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  placeholder="e.g. 10mg (1 tablet)"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Caregiver Confirmed Spoken Instructions *
                </label>
                <textarea
                  rows={2}
                  value={confirmedInstruction}
                  onChange={(e) => setConfirmedInstruction(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none leading-relaxed"
                  placeholder="e.g. Take with a glass of water after breakfast"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Current Quantity *
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={quantityRemaining}
                    onChange={(e) => setQuantityRemaining(parseInt(e.target.value) || 0)}
                    required
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Low Stock Threshold *
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={lowStockThreshold}
                    onChange={(e) => setLowStockThreshold(parseInt(e.target.value) || 5)}
                    required
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Call Reminder Time *
                  </label>
                  <input
                    type="time"
                    value={reminderTime}
                    onChange={(e) => setReminderTime(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm font-mono focus:ring-2 focus:ring-green-500 outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Frequency
                  </label>
                  <select
                    value={frequency}
                    onChange={(e) => setFrequency(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  >
                    <option value="DAILY">Daily</option>
                    <option value="WEEKDAYS">Weekdays Only</option>
                  </select>
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-green-600 hover:bg-green-500 text-white font-bold text-xs rounded-xl shadow-md transition"
                >
                  Save &amp; Schedule Calls
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Prescription OCR Modal */}
      <PrescriptionOcrModal
        patients={patients}
        isOpen={isOcrOpen}
        onClose={() => setIsOcrOpen(false)}
        onMedicationCreated={() => loadData()}
      />
    </div>
  );
};
