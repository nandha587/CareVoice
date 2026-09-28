import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { DashboardSummary, DoseEvent, Patient } from '../types';
import { StatCard } from '../components/StatCard';
import { DoseStatusBadge } from '../components/DoseStatusBadge';
import { InteractivePhoneSimulator } from '../components/InteractivePhoneSimulator';
import { PrescriptionOcrModal } from '../components/PrescriptionOcrModal';
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  PhoneForwarded,
  Sparkles,
  RefreshCw,
  Bell,
  FileText,
  UserPlus
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedCallDose, setSelectedCallDose] = useState<DoseEvent | null>(null);
  const [isOcrOpen, setIsOcrOpen] = useState<boolean>(false);
  const [refreshing, setRefreshing] = useState<boolean>(false);

  const loadData = async () => {
    try {
      const [sumData, patData] = await Promise.all([
        api.getDashboardSummary(),
        api.getPatients(),
      ]);
      setSummary(sumData);
      setPatients(patData);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
    // Auto refresh every 30 seconds
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleTriggerScheduler = async () => {
    setRefreshing(true);
    try {
      await api.triggerScheduler();
      await loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to trigger scheduler');
      setRefreshing(false);
    }
  };

  const handleCallCompleted = (_updated: DoseEvent) => {
    loadData();
  };

  if (loading && !summary) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12 flex justify-center">
        <div className="flex flex-col items-center space-y-3">
          <div className="w-10 h-10 border-4 border-green-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-slate-500">Loading adherence dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm">
        <div>
          <span className="inline-flex items-center text-xs font-semibold px-2.5 py-0.5 rounded-full bg-green-100 text-green-800 mb-2">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 mr-1.5 animate-pulse"></span>
            Automated Phone Engine Active
          </span>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Today's Medication Adherence</h1>
          <p className="text-sm text-slate-500 mt-1">
            Monitoring scheduled phone calls and senior keypad confirmations in real time.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => setIsOcrOpen(true)}
            className="px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold rounded-xl transition shadow flex items-center space-x-1.5"
          >
            <FileText className="w-4 h-4 text-green-400" />
            <span>Scan Prescription (OCR)</span>
          </button>

          <button
            onClick={handleTriggerScheduler}
            disabled={refreshing}
            className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition flex items-center space-x-1.5"
            title="Scan for due reminder phone calls now"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
            <span>Heartbeat Scan</span>
          </button>

          <Link
            to="/patients"
            className="px-4 py-2.5 bg-green-600 hover:bg-green-500 text-white text-xs font-bold rounded-xl transition shadow flex items-center space-x-1.5"
          >
            <UserPlus className="w-4 h-4" />
            <span>Manage Patients</span>
          </Link>
        </div>
      </div>

      {/* Primary Adherence Metrics */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard
          title="Doses Taken"
          value={summary?.todayTaken ?? 0}
          subtitle="Confirmed via keypad '1'"
          icon={CheckCircle2}
          color="green"
        />
        <StatCard
          title="Missed Doses"
          value={summary?.todayMissed ?? 0}
          subtitle="Reported via keypad '2'"
          icon={XCircle}
          color="red"
        />
        <StatCard
          title="No Response"
          value={summary?.todayNoResponse ?? 0}
          subtitle="Calls unanswered"
          icon={AlertTriangle}
          color="amber"
        />
        <StatCard
          title="Upcoming Today"
          value={summary?.todayScheduled ?? 0}
          subtitle="Pending scheduled call"
          icon={Clock}
          color="blue"
        />
        <StatCard
          title="7-Day Adherence"
          value={`${summary?.weeklyAdherencePercentage ?? 100}%`}
          subtitle="Weekly patient average"
          icon={Sparkles}
          color="slate"
        />
      </div>

      {/* Main Content Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Today's Scheduled Reminders Table (2 cols wide) */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Clock className="w-5 h-5 text-slate-500" />
                <h2 className="font-bold text-slate-900 text-base">Today's Reminder Schedule</h2>
              </div>
              <span className="text-xs font-semibold text-slate-400">
                {summary?.todayDoses.length ?? 0} scheduled event(s)
              </span>
            </div>

            {summary?.todayDoses.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-sm">
                No reminders scheduled for today. Add a medication schedule to start automated calls.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-600">
                  <thead className="bg-slate-50/80 text-[11px] uppercase font-bold text-slate-400 tracking-wider border-b border-slate-100">
                    <tr>
                      <th className="px-6 py-3">Scheduled Time</th>
                      <th className="px-6 py-3">Elderly Patient</th>
                      <th className="px-6 py-3">Medication</th>
                      <th className="px-6 py-3">Status</th>
                      <th className="px-6 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {summary?.todayDoses.map((dose) => (
                      <tr key={dose.id} className="hover:bg-slate-50/80 transition">
                        <td className="px-6 py-4 font-mono font-semibold text-slate-800 text-xs">
                          {new Date(dose.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-bold text-slate-900">{dose.patientName}</div>
                          <div className="text-xs text-slate-400 font-mono">{dose.patientPhone}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-semibold text-slate-800">{dose.medicationName}</div>
                          <div className="text-xs text-slate-400">{dose.dosage}</div>
                        </td>
                        <td className="px-6 py-4">
                          <DoseStatusBadge status={dose.status} />
                        </td>
                        <td className="px-6 py-4 text-right">
                          <button
                            onClick={() => setSelectedCallDose(dose)}
                            className="inline-flex items-center px-3 py-1.5 bg-green-50 hover:bg-green-100 text-green-700 text-xs font-bold rounded-xl transition border border-green-200"
                            title="Test this phone call in the simulator"
                          >
                            <PhoneForwarded className="w-3.5 h-3.5 mr-1" />
                            <span>Test Call</span>
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Low Stock Warning Banner Card */}
          {summary?.lowStockMedications && summary.lowStockMedications.length > 0 && (
            <div className="bg-amber-50 border-2 border-amber-200 rounded-3xl p-6 shadow-sm">
              <div className="flex items-center space-x-2 text-amber-900 font-bold mb-3">
                <AlertTriangle className="w-5 h-5 text-amber-600" />
                <h3 className="text-base">Medication Low Stock Warnings</h3>
              </div>
              <p className="text-xs text-amber-800 mb-4">
                The following medications have reached or fallen below their configured caregiver threshold:
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {summary.lowStockMedications.map((med) => (
                  <div key={med.id} className="bg-white p-3.5 rounded-2xl border border-amber-200 shadow-xs flex items-center justify-between">
                    <div>
                      <span className="font-bold text-slate-900 text-sm">{med.name}</span>
                      <p className="text-xs text-slate-500">Patient: {med.patientName}</p>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-bold text-red-600 bg-red-50 border border-red-200 px-2 py-0.5 rounded-lg">
                        {med.quantityRemaining} remaining
                      </span>
                      <p className="text-[10px] text-slate-400 mt-1">Threshold: {med.lowStockThreshold}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Live Phone Handset Simulator & Recent Alerts (1 col) */}
        <div className="space-y-6">
          {/* Interactive Phone Simulator Widget */}
          <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
              <div className="flex items-center space-x-2">
                <PhoneForwarded className="w-5 h-5 text-green-600" />
                <h3 className="font-bold text-slate-900 text-base">Elderly Phone Simulator</h3>
              </div>
              <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 bg-slate-100 rounded text-slate-500">
                Interactive
              </span>
            </div>

            {selectedCallDose ? (
              <InteractivePhoneSimulator
                doseEvent={selectedCallDose}
                onCallCompleted={handleCallCompleted}
              />
            ) : summary?.todayDoses && summary.todayDoses.length > 0 ? (
              <InteractivePhoneSimulator
                doseEvent={summary.todayDoses[0]}
                onCallCompleted={handleCallCompleted}
              />
            ) : (
              <div className="p-8 text-center text-slate-400 text-xs">
                Select a dose from the schedule above to test live voice prompts and keypad adherence.
              </div>
            )}
          </div>

          {/* Recent Alerts Feed */}
          <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
              <div className="flex items-center space-x-2">
                <Bell className="w-5 h-5 text-slate-600" />
                <h3 className="font-bold text-slate-900 text-base">Caregiver Alerts</h3>
              </div>
              <Link to="/reports" className="text-xs font-semibold text-green-600 hover:underline">
                View Reports &rarr;
              </Link>
            </div>

            {summary?.recentAlerts.length === 0 ? (
              <p className="text-xs text-slate-400 py-4 text-center">No alerts triggered today.</p>
            ) : (
              <div className="space-y-3">
                {summary?.recentAlerts.map((alert) => (
                  <div key={alert.id} className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <div className="flex items-center justify-between font-bold">
                      <span className={alert.type.includes('MISSED') || alert.type.includes('NO_RESPONSE') ? 'text-red-600' : 'text-amber-600'}>
                        {alert.type.replace(/_/g, ' ')}
                      </span>
                      <span className="text-[10px] text-slate-400 font-normal">
                        {new Date(alert.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <p className="text-slate-600 mt-1 leading-relaxed">{alert.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

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
