import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { DoseEvent } from '../types';
import { InteractivePhoneSimulator } from '../components/InteractivePhoneSimulator';
import { PhoneForwarded, Info, RefreshCw } from 'lucide-react';

export const TelephonySimulatorPage: React.FC = () => {
  const [doses, setDoses] = useState<DoseEvent[]>([]);
  const [selectedDose, setSelectedDose] = useState<DoseEvent | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const loadDoses = async () => {
    try {
      const data = await api.getTodayDoses();
      setDoses(data);
      if (data.length > 0 && !selectedDose) {
        setSelectedDose(data[0]);
      }
    } catch (err) {
      console.error('Failed to load doses:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDoses();
  }, []);

  const handleCallFinished = (updated: DoseEvent) => {
    setSelectedDose(updated);
    loadDoses();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Header */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold uppercase tracking-wider text-green-700 bg-green-50 px-2.5 py-0.5 rounded-full mb-2 inline-block">
            Telephony &amp; DTMF Sandbox
          </span>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center space-x-2">
            <PhoneForwarded className="w-6 h-6 text-green-600" />
            <span>Interactive Senior Telephone Simulator</span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Simulate outbound automated calls, multilingual voice synthesis, and keypad responses (1 = Taken, 2 = Missed).
          </p>
        </div>

        <button
          onClick={loadDoses}
          className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition flex items-center space-x-1.5 self-start md:self-auto"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Refresh Reminder Queue</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Side: Dose Selection List (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-200/80 p-5 shadow-sm">
            <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider text-slate-400 mb-3">
              Select Dose Reminder to Dial:
            </h2>

            {loading ? (
              <p className="text-xs text-slate-400 py-6 text-center">Loading reminder queue...</p>
            ) : doses.length === 0 ? (
              <div className="text-center py-8 text-xs text-slate-400">
                No reminders available today. Create a patient and medication schedule to test dialing.
              </div>
            ) : (
              <div className="space-y-2.5 max-h-[500px] overflow-y-auto pr-1">
                {doses.map((dose) => {
                  const isSelected = selectedDose?.id === dose.id;
                  return (
                    <div
                      key={dose.id}
                      onClick={() => setSelectedDose(dose)}
                      className={`p-4 rounded-2xl border cursor-pointer transition ${
                        isSelected
                          ? 'border-green-500 bg-green-50/50 shadow-sm'
                          : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50'
                      }`}
                    >
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-bold text-slate-900">{dose.patientName}</span>
                        <span className="font-mono text-slate-500 font-semibold">
                          {new Date(dose.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                      <div className="text-xs text-slate-600 mt-1 flex items-center justify-between">
                        <span>
                          {dose.medicationName} ({dose.dosage})
                        </span>
                        <span className={`text-[10px] uppercase font-bold px-2 py-0.5 rounded ${
                          dose.status === 'TAKEN'
                            ? 'bg-green-100 text-green-700'
                            : dose.status === 'MISSED'
                            ? 'bg-red-100 text-red-700'
                            : 'bg-slate-100 text-slate-600'
                        }`}>
                          {dose.status}
                        </span>
                      </div>
                      <div className="text-[11px] text-slate-400 mt-2 flex items-center justify-between">
                        <span>Voice: {dose.preferredLanguage}</span>
                        <span>Attempts: {dose.callAttempts}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Explanation Box */}
          <div className="bg-slate-50 p-5 rounded-3xl border border-slate-200/80 text-xs text-slate-600 space-y-3 leading-relaxed">
            <div className="font-bold text-slate-900 flex items-center space-x-1.5">
              <Info className="w-4 h-4 text-green-600" />
              <span>How Senior Voice Calling Works:</span>
            </div>
            <ol className="list-decimal pl-4 space-y-1.5 text-slate-600">
              <li>The Spring Boot scheduler triggers an automated outbound call when the reminder is due.</li>
              <li>The elderly patient hears a gentle voice reminder spoken in their preferred language.</li>
              <li>Senior presses <strong>1</strong> on their keypad: Confirmed Taken &rarr; inventory decreases by 1.</li>
              <li>Senior presses <strong>2</strong> on their keypad: Reported Missed &rarr; caregiver alerted immediately.</li>
              <li>No answer after configured retries: System escalates with an urgent No-Response notification.</li>
            </ol>
          </div>
        </div>

        {/* Right Side: Visual Phone Handset (7 cols) */}
        <div className="lg:col-span-7 flex flex-col items-center">
          {selectedDose ? (
            <div className="w-full max-w-md">
              <InteractivePhoneSimulator
                doseEvent={selectedDose}
                onCallCompleted={handleCallFinished}
              />
            </div>
          ) : (
            <div className="bg-white p-12 rounded-3xl border border-slate-200 text-center text-slate-400 text-xs w-full max-w-md">
              Select a reminder from the left queue to initiate simulated cellular audio and DTMF responses.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
