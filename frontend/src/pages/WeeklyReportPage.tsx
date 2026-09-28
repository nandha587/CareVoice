import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Patient, WeeklyReport } from '../types';
import { BarChart3, ShieldCheck, CheckCircle2, XCircle, AlertTriangle, AlertCircle, Printer, Calendar } from 'lucide-react';

export const WeeklyReportPage: React.FC = () => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [report, setReport] = useState<WeeklyReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    api.getPatients()
      .then((data) => {
        setPatients(data);
        if (data.length > 0) {
          setSelectedPatientId(data[0].id);
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (selectedPatientId) {
      setLoading(true);
      api.getWeeklyReport(selectedPatientId)
        .then(setReport)
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [selectedPatientId]);

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header & Patient Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm print:hidden">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center space-x-2">
            <BarChart3 className="w-6 h-6 text-green-600" />
            <span>Weekly Adherence Summary Report</span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Objective, factual log of scheduled reminders, keypad responses, and stock status.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <select
            value={selectedPatientId}
            onChange={(e) => setSelectedPatientId(e.target.value)}
            className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:ring-2 focus:ring-green-500 outline-none"
          >
            {patients.map((p) => (
              <option key={p.id} value={p.id}>
                {p.fullName} ({p.preferredLanguage})
              </option>
            ))}
          </select>

          <button
            onClick={() => window.print()}
            className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold rounded-xl flex items-center space-x-1.5 transition"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Report</span>
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-400">Loading adherence calculations...</div>
      ) : !report ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 text-slate-400">
          No report data available for the selected patient.
        </div>
      ) : (
        <div className="bg-white rounded-3xl p-8 border border-slate-200 shadow-sm space-y-8 print:p-0 print:border-none print:shadow-none">
          {/* Patient & Report Range Card */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-slate-100 pb-6 gap-4">
            <div>
              <span className="text-xs uppercase font-bold text-green-700 tracking-wider">Patient Summary</span>
              <h2 className="text-2xl font-extrabold text-slate-900">{report.patientName}</h2>
            </div>
            <div className="text-right">
              <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">Audit Timeframe</span>
              <div className="flex items-center space-x-1.5 text-xs text-slate-600 font-mono mt-0.5">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>{report.periodStart} &rarr; {report.periodEnd}</span>
              </div>
            </div>
          </div>

          {/* Adherence Score Card */}
          <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-2xl p-6 border border-green-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-green-800">
                Weekly Confirmed Adherence
              </p>
              <p className="text-4xl font-black text-green-700 mt-1">
                {report.adherencePercentage}%
              </p>
              <p className="text-xs text-green-900/80 mt-2 font-medium">
                {report.summaryNarrative}
              </p>
            </div>
            <div className="shrink-0 w-24 h-24 rounded-full bg-white border-4 border-green-400 flex items-center justify-center font-bold text-green-700 text-lg shadow-inner">
              {report.adherencePercentage}%
            </div>
          </div>

          {/* Breakdown Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200">
              <span className="text-[11px] font-bold uppercase text-slate-500 block">Total Scheduled</span>
              <p className="text-2xl font-extrabold text-slate-900 mt-1">{report.totalScheduledDoses}</p>
              <p className="text-[10px] text-slate-400 mt-0.5">Automated calls dialed</p>
            </div>

            <div className="p-4 rounded-2xl bg-green-50 border border-green-200">
              <span className="text-[11px] font-bold uppercase text-green-700 block flex items-center">
                <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                Confirmed Taken
              </span>
              <p className="text-2xl font-extrabold text-green-800 mt-1">{report.dosesTaken}</p>
              <p className="text-[10px] text-green-600 mt-0.5">Keypad '1' recorded</p>
            </div>

            <div className="p-4 rounded-2xl bg-red-50 border border-red-200">
              <span className="text-[11px] font-bold uppercase text-red-700 block flex items-center">
                <XCircle className="w-3.5 h-3.5 mr-1" />
                Reported Missed
              </span>
              <p className="text-2xl font-extrabold text-red-800 mt-1">{report.dosesMissed}</p>
              <p className="text-[10px] text-red-600 mt-0.5">Keypad '2' pressed</p>
            </div>

            <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200">
              <span className="text-[11px] font-bold uppercase text-amber-700 block flex items-center">
                <AlertTriangle className="w-3.5 h-3.5 mr-1" />
                No Response
              </span>
              <p className="text-2xl font-extrabold text-amber-800 mt-1">{report.dosesNoResponse}</p>
              <p className="text-[10px] text-amber-600 mt-0.5">All retries unanswered</p>
            </div>
          </div>

          {/* Low Stock Status Section */}
          <div className="border-t border-slate-100 pt-6">
            <h3 className="text-sm font-bold text-slate-800 mb-3 flex items-center space-x-2">
              <AlertCircle className="w-4 h-4 text-amber-600" />
              <span>Inventory &amp; Refill Status</span>
            </h3>

            {report.lowStockWarnings.length === 0 ? (
              <div className="p-4 bg-slate-50 rounded-2xl text-xs text-slate-600 flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-green-600" />
                <span>All prescribed medications for this patient have sufficient stock above their configured thresholds.</span>
              </div>
            ) : (
              <div className="space-y-2">
                {report.lowStockWarnings.map((warning, idx) => (
                  <div key={idx} className="p-3 bg-red-50 border border-red-200 text-red-800 rounded-xl text-xs font-medium">
                    ⚠️ {warning}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Mandatory Healthcare Disclaimer */}
          <div className="border-t border-slate-100 pt-6 flex items-start space-x-3 text-slate-400 text-xs leading-relaxed">
            <ShieldCheck className="w-5 h-5 text-slate-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-slate-600">CareVoice Compliance &amp; Safety Disclaimer:</p>
              <p className="mt-0.5">
                This document represents an objective record of automated telephone reminder responses and is not a medical diagnosis, clinical evaluation, or assessment of health improvement. Dosages and medication instructions must be validated by a licensed physician or pharmacist.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
