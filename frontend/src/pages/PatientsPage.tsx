import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Patient } from '../types';
import { Users, UserPlus, Phone, Globe, Clock, Edit2, Trash2, X } from 'lucide-react';

export const PatientsPage: React.FC = () => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null);

  // Form states
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [preferredLanguage, setPreferredLanguage] = useState('en-US');
  const [timezone, setTimezone] = useState('America/New_York');
  const [emergencyContact, setEmergencyContact] = useState('');
  const [notes, setNotes] = useState('');

  const loadPatients = async () => {
    try {
      const data = await api.getPatients();
      setPatients(data);
    } catch (err) {
      console.error('Failed to load patients:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPatients();
  }, []);

  const openCreateModal = () => {
    setEditingPatient(null);
    setFullName('');
    setPhoneNumber('');
    setPreferredLanguage('en-US');
    setTimezone('America/New_York');
    setEmergencyContact('');
    setNotes('');
    setIsModalOpen(true);
  };

  const openEditModal = (p: Patient) => {
    setEditingPatient(p);
    setFullName(p.fullName);
    setPhoneNumber(p.phoneNumber);
    setPreferredLanguage(p.preferredLanguage);
    setTimezone(p.timezone);
    setEmergencyContact(p.emergencyContact || '');
    setNotes(p.notes || '');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingPatient) {
        await api.updatePatient(editingPatient.id, {
          fullName,
          phoneNumber,
          preferredLanguage,
          timezone,
          emergencyContact,
          notes,
        });
      } else {
        await api.createPatient({
          fullName,
          phoneNumber,
          preferredLanguage,
          timezone,
          emergencyContact,
          notes,
        });
      }
      setIsModalOpen(false);
      loadPatients();
    } catch (err: any) {
      alert(err.message || 'Operation failed');
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!window.confirm(`Are you sure you want to remove patient profile: ${name}?`)) return;
    try {
      await api.deletePatient(id);
      loadPatients();
    } catch (err: any) {
      alert(err.message || 'Failed to delete patient');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center space-x-2">
            <Users className="w-6 h-6 text-green-600" />
            <span>Elderly Patient Profiles</span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Caregivers can manage profiles, phone numbers, and language preferences. No smartphones required for seniors.
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="px-5 py-2.5 bg-green-600 hover:bg-green-500 text-white font-bold text-xs rounded-xl shadow-lg shadow-green-900/20 flex items-center space-x-2 transition"
        >
          <UserPlus className="w-4 h-4" />
          <span>Add New Patient</span>
        </button>
      </div>

      {/* Patients Grid */}
      {loading ? (
        <div className="text-center py-12 text-slate-400">Loading patient profiles...</div>
      ) : patients.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-sm">
          <Users className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="font-bold text-slate-800 text-base">No elderly patients enrolled yet</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            Add your elderly loved one to configure automated voice reminders in their preferred language.
          </p>
          <button
            onClick={openCreateModal}
            className="mt-4 px-4 py-2 bg-green-600 text-white text-xs font-bold rounded-xl"
          >
            Add First Patient
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {patients.map((patient) => (
            <div
              key={patient.id}
              className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div className="w-12 h-12 rounded-2xl bg-green-50 text-green-700 flex items-center justify-center font-bold text-lg border border-green-100">
                    {patient.fullName.charAt(0)}
                  </div>
                  <div className="flex items-center space-x-1">
                    <button
                      onClick={() => openEditModal(patient)}
                      className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition"
                      title="Edit Profile"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(patient.id, patient.fullName)}
                      className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                      title="Delete Profile"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <div className="mt-4">
                  <h3 className="text-lg font-bold text-slate-900">{patient.fullName}</h3>
                  <div className="mt-2 space-y-1.5 text-xs text-slate-600">
                    <div className="flex items-center space-x-2">
                      <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span className="font-mono">{patient.phoneNumber}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Globe className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span>
                        Voice Language:{' '}
                        <strong className="text-slate-800">
                          {patient.preferredLanguage === 'hi-IN'
                            ? 'Hindi (हिंदी)'
                            : patient.preferredLanguage === 'es-ES'
                            ? 'Spanish (Español)'
                            : 'English (US)'}
                        </strong>
                      </span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span>Timezone: {patient.timezone}</span>
                    </div>
                  </div>

                  {patient.emergencyContact && (
                    <div className="mt-3 p-2.5 bg-slate-50 rounded-xl border border-slate-100 text-xs">
                      <span className="font-semibold text-slate-500 text-[10px] uppercase block">Emergency Contact:</span>
                      <p className="text-slate-700 mt-0.5">{patient.emergencyContact}</p>
                    </div>
                  )}

                  {patient.notes && (
                    <p className="text-xs text-slate-500 italic mt-3 line-clamp-2">
                      "{patient.notes}"
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-xs">
                <span className="text-slate-400 text-[11px]">
                  Enrolled {new Date(patient.createdAt).toLocaleDateString()}
                </span>
                <span className="font-semibold text-green-600 bg-green-50 px-2.5 py-1 rounded-lg">
                  Voice Calling Active
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Patient Create/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <h3 className="text-lg font-bold text-slate-900">
                {editingPatient ? 'Edit Patient Profile' : 'Enrol New Elderly Patient'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Full Name *
                </label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  placeholder="e.g. Eleanor Vance"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Phone Number (E.164 Format) *
                </label>
                <input
                  type="tel"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm font-mono focus:ring-2 focus:ring-green-500 outline-none"
                  placeholder="e.g. +15551234567"
                />
                <p className="text-[11px] text-slate-400 mt-1">Must include country code, e.g. +1 for US/Canada, +91 for India.</p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Spoken Voice Language
                  </label>
                  <select
                    value={preferredLanguage}
                    onChange={(e) => setPreferredLanguage(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  >
                    <option value="en-US">English (US)</option>
                    <option value="es-ES">Spanish (Español)</option>
                    <option value="hi-IN">Hindi (हिंदी)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Timezone
                  </label>
                  <select
                    value={timezone}
                    onChange={(e) => setTimezone(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  >
                    <option value="America/New_York">America/New_York (EST)</option>
                    <option value="America/Chicago">America/Chicago (CST)</option>
                    <option value="America/Los_Angeles">America/Los_Angeles (PST)</option>
                    <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                    <option value="Europe/London">Europe/London (GMT)</option>
                    <option value="UTC">UTC</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Emergency Contact Details
                </label>
                <input
                  type="text"
                  value={emergencyContact}
                  onChange={(e) => setEmergencyContact(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  placeholder="e.g. Sarah Miller (Daughter) - +15559876543"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Caregiver Notes &amp; Medical Context
                </label>
                <textarea
                  rows={2}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-green-500 outline-none"
                  placeholder="e.g. Mild hearing impairment, speaks gently."
                />
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-green-600 hover:bg-green-500 text-white font-bold text-xs rounded-xl shadow-md transition"
                >
                  {editingPatient ? 'Save Changes' : 'Save Patient Profile'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
