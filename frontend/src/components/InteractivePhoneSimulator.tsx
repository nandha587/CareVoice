import React, { useState } from 'react';
import { DoseEvent, SimulatedCallResult } from '../types';
import { api } from '../api/client';
import { Phone, PhoneCall, PhoneOff, Volume2, CheckCircle2, AlertTriangle, RotateCcw } from 'lucide-react';

interface SimulatorProps {
  doseEvent: DoseEvent;
  onCallCompleted?: (updated: DoseEvent) => void;
}

export const InteractivePhoneSimulator: React.FC<SimulatorProps> = ({ doseEvent, onCallCompleted }) => {
  const [callState, setCallState] = useState<'IDLE' | 'RINGING' | 'CONNECTED' | 'COMPLETED'>('IDLE');
  const [spokenText, setSpokenText] = useState<string>('');
  const [callResult, setCallResult] = useState<SimulatedCallResult | null>(null);
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);

  const startCall = () => {
    setCallState('RINGING');
    setCallResult(null);

    // Realistic telephone ring delay
    setTimeout(() => {
      setCallState('CONNECTED');
      // Generate prompt text
      const prompt = getSpokenScript(doseEvent);
      setSpokenText(prompt);
      speakText(prompt, doseEvent.preferredLanguage);
    }, 1500);
  };

  const getSpokenScript = (event: DoseEvent) => {
    const lang = event.preferredLanguage || 'en-US';
    if (lang.startsWith('es')) {
      return `Hola ${event.patientName}. Este es su recordatorio de CareVoice. Es hora de tomar su medicina: ${event.medicationName || 'medicamento'}, dosis ${event.dosage || '1 tableta'}. ${event.confirmedInstruction || 'Tomar con agua'}. Por favor presione 1 después de tomar su medicina. Presione 2 si aún no la ha tomado.`;
    }
    if (lang.startsWith('hi')) {
      return `Namaste ${event.patientName} ji. Yeh aapka CareVoice dawai reminder hai. Aapki dawai ka samay ho gaya hai: ${event.medicationName || 'dawai'}, matra ${event.dosage || '1 goli'}. ${event.confirmedInstruction || 'Pani ke sath lein'}. Dawai lene ke baad kripya 1 dabayein. Agar dawai nahi li hai toh 2 dabayein.`;
    }
    return `Hello ${event.patientName}. This is your CareVoice medication reminder. It is time for your scheduled medicine: ${event.medicationName || 'prescription'}, dosage ${event.dosage || '1 dose'}. ${event.confirmedInstruction || 'Take with water'}. Please press 1 after taking your medicine. Press 2 if you have not taken it yet.`;
  };

  const speakText = (text: string, lang: string) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = lang || 'en-US';
      utterance.rate = 0.9; // gentle cadence for elderly ears
      utterance.onstart = () => setIsSpeaking(true);
      utterance.onend = () => setIsSpeaking(false);
      utterance.onerror = () => setIsSpeaking(false);
      window.speechSynthesis.speak(utterance);
    }
  };

  const handleKeypadPress = async (digit: string) => {
    if (callState !== 'CONNECTED' || loading) return;
    if ('speechSynthesis' in window) window.speechSynthesis.cancel();

    setLoading(true);
    try {
      const res = await api.simulateCall(doseEvent.id, digit);
      setCallResult(res);
      setCallState('COMPLETED');
      if (onCallCompleted) onCallCompleted(res.updatedEvent);

      // Speak response
      let speech = "Thank you. Your response is recorded.";
      if (digit === '1') speech = "Thank you. Your medication is recorded as taken. Stay healthy. Goodbye.";
      if (digit === '2') speech = "We have recorded that you have not taken your dose. We will notify your caregiver. Please rest. Goodbye.";
      speakText(speech, doseEvent.preferredLanguage);
    } catch (err: any) {
      alert(err.message || 'Call simulation error');
    } finally {
      setLoading(false);
    }
  };

  const resetCall = () => {
    if ('speechSynthesis' in window) window.speechSynthesis.cancel();
    setCallState('IDLE');
    setCallResult(null);
    setSpokenText('');
  };

  return (
    <div className="bg-slate-900 text-white rounded-3xl p-6 shadow-2xl border border-slate-800 max-w-sm mx-auto">
      {/* Phone Screen Display */}
      <div className="bg-slate-950 rounded-2xl p-4 border border-slate-800 mb-6 text-center">
        <div className="flex justify-between items-center text-[10px] font-mono text-slate-400 mb-2">
          <span>CAREVOICE CELLULAR</span>
          <span className="flex items-center space-x-1">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-ping"></span>
            <span>HD VOICE</span>
          </span>
        </div>

        <div className="my-3">
          <p className="text-xs uppercase font-medium tracking-widest text-slate-400">Calling Patient</p>
          <p className="text-xl font-bold text-white tracking-tight">{doseEvent.patientName}</p>
          <p className="text-xs font-mono text-slate-400">{doseEvent.patientPhone} ({doseEvent.preferredLanguage})</p>
          <p className="text-xs text-green-400 font-semibold mt-1">
            {doseEvent.medicationName} — {doseEvent.dosage}
          </p>
        </div>

        {/* Call Status Badge */}
        <div className="mt-2">
          {callState === 'IDLE' && (
            <span className="inline-flex items-center text-xs font-medium text-slate-400 bg-slate-900 px-3 py-1 rounded-full">
              Handset Ready
            </span>
          )}
          {callState === 'RINGING' && (
            <span className="inline-flex items-center text-xs font-medium text-amber-300 bg-amber-950/80 px-3 py-1 rounded-full animate-bounce">
              <PhoneCall className="w-3.5 h-3.5 mr-1 animate-spin" />
              Ringing Outbound...
            </span>
          )}
          {callState === 'CONNECTED' && (
            <span className="inline-flex items-center text-xs font-medium text-green-400 bg-green-950/80 px-3 py-1 rounded-full">
              <Volume2 className={`w-3.5 h-3.5 mr-1 ${isSpeaking ? 'animate-pulse text-green-300' : ''}`} />
              Call in Progress (Speaking Prompt)
            </span>
          )}
          {callState === 'COMPLETED' && (
            <span className="inline-flex items-center text-xs font-medium text-slate-300 bg-slate-800 px-3 py-1 rounded-full">
              Call Disconnected
            </span>
          )}
        </div>

        {/* Live Audio Transcript Box */}
        {spokenText && (
          <div className="mt-4 p-3 bg-slate-900/90 rounded-xl text-left border border-slate-800">
            <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider flex items-center justify-between">
              <span>Text-To-Speech Audio Script:</span>
              {isSpeaking && <span className="text-green-400 text-[10px] animate-pulse">● Speaking...</span>}
            </p>
            <p className="text-xs text-slate-200 mt-1 italic leading-relaxed">
              "{spokenText}"
            </p>
          </div>
        )}

        {/* Post-Call Result Message */}
        {callResult && (
          <div className={`mt-3 p-3 rounded-xl text-left border text-xs ${
            callResult.status === 'TAKEN'
              ? 'bg-green-950/60 border-green-800/80 text-green-200'
              : 'bg-red-950/60 border-red-800/80 text-red-200'
          }`}>
            <div className="flex items-center font-bold mb-1">
              {callResult.status === 'TAKEN' ? (
                <CheckCircle2 className="w-4 h-4 mr-1 text-green-400" />
              ) : (
                <AlertTriangle className="w-4 h-4 mr-1 text-red-400" />
              )}
              <span>Status Updated: {callResult.status}</span>
            </div>
            <p className="text-[11px] opacity-90">{callResult.patientResponse}</p>
          </div>
        )}
      </div>

      {/* Interactive DTMF Keypad (Large accessible buttons for senior telephony simulation) */}
      <div className="grid grid-cols-3 gap-3 mb-6">
        {/* Key 1: Dose Taken */}
        <button
          onClick={() => handleKeypadPress('1')}
          disabled={callState !== 'CONNECTED' || loading}
          className={`h-16 rounded-2xl flex flex-col items-center justify-center transition border ${
            callState === 'CONNECTED'
              ? 'bg-green-700/80 hover:bg-green-600 text-white border-green-500 shadow-lg shadow-green-900/40 active:scale-95'
              : 'bg-slate-800/50 text-slate-500 border-slate-800 cursor-not-allowed'
          }`}
        >
          <span className="text-xl font-bold leading-none">1</span>
          <span className="text-[9px] uppercase font-semibold text-green-200 mt-1">Dose Taken</span>
        </button>

        {/* Key 2: Dose Not Taken */}
        <button
          onClick={() => handleKeypadPress('2')}
          disabled={callState !== 'CONNECTED' || loading}
          className={`h-16 rounded-2xl flex flex-col items-center justify-center transition border ${
            callState === 'CONNECTED'
              ? 'bg-red-700/80 hover:bg-red-600 text-white border-red-500 shadow-lg shadow-red-900/40 active:scale-95'
              : 'bg-slate-800/50 text-slate-500 border-slate-800 cursor-not-allowed'
          }`}
        >
          <span className="text-xl font-bold leading-none">2</span>
          <span className="text-[9px] uppercase font-semibold text-red-200 mt-1">Not Taken</span>
        </button>

        {/* Timeout / No Response Button */}
        <button
          onClick={() => handleKeypadPress('TIMEOUT')}
          disabled={callState !== 'CONNECTED' || loading}
          className={`h-16 rounded-2xl flex flex-col items-center justify-center transition border ${
            callState === 'CONNECTED'
              ? 'bg-amber-700/80 hover:bg-amber-600 text-white border-amber-500 shadow-lg shadow-amber-900/40 active:scale-95'
              : 'bg-slate-800/50 text-slate-500 border-slate-800 cursor-not-allowed'
          }`}
        >
          <span className="text-xl font-bold leading-none">0</span>
          <span className="text-[9px] uppercase font-semibold text-amber-200 mt-1">No Answer</span>
        </button>
      </div>

      {/* Call Control Action Bar */}
      <div className="flex items-center justify-between space-x-3">
        {callState === 'IDLE' && (
          <button
            onClick={startCall}
            className="w-full py-3.5 bg-green-600 hover:bg-green-500 text-white font-bold rounded-2xl flex items-center justify-center space-x-2 shadow-lg shadow-green-950 transition active:scale-98"
          >
            <Phone className="w-5 h-5 fill-current" />
            <span>Place Automated Call</span>
          </button>
        )}

        {(callState === 'RINGING' || callState === 'CONNECTED') && (
          <button
            onClick={() => handleKeypadPress('TIMEOUT')}
            className="w-full py-3.5 bg-red-600 hover:bg-red-500 text-white font-bold rounded-2xl flex items-center justify-center space-x-2 shadow-lg shadow-red-950 transition active:scale-98"
          >
            <PhoneOff className="w-5 h-5" />
            <span>Hang Up (No Answer)</span>
          </button>
        )}

        {callState === 'COMPLETED' && (
          <button
            onClick={resetCall}
            className="w-full py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 font-semibold rounded-2xl flex items-center justify-center space-x-2 transition"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Simulate Another Call</span>
          </button>
        )}
      </div>

      <p className="text-[10px] text-slate-400 text-center mt-3">
        *Press 1 or 2 to test the instant database update, adherence tracking, and alert triggers.
      </p>
    </div>
  );
};
