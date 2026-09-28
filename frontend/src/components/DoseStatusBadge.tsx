import React from 'react';
import { DoseStatus } from '../types';
import { CheckCircle2, XCircle, Clock, AlertTriangle, PhoneCall } from 'lucide-react';

interface DoseStatusBadgeProps {
  status: DoseStatus;
  size?: 'sm' | 'md';
}

export const DoseStatusBadge: React.FC<DoseStatusBadgeProps> = ({ status, size = 'md' }) => {
  const isSm = size === 'sm';

  switch (status) {
    case 'TAKEN':
      return (
        <span className={`inline-flex items-center font-medium bg-green-100 text-green-800 rounded-full ${isSm ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'}`}>
          <CheckCircle2 className={`mr-1 ${isSm ? 'w-3 h-3' : 'w-3.5 h-3.5'} text-green-600`} />
          Taken
        </span>
      );
    case 'MISSED':
      return (
        <span className={`inline-flex items-center font-medium bg-red-100 text-red-800 rounded-full ${isSm ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'}`}>
          <XCircle className={`mr-1 ${isSm ? 'w-3 h-3' : 'w-3.5 h-3.5'} text-red-600`} />
          Missed
        </span>
      );
    case 'NO_RESPONSE':
      return (
        <span className={`inline-flex items-center font-medium bg-amber-100 text-amber-800 rounded-full ${isSm ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'}`}>
          <AlertTriangle className={`mr-1 ${isSm ? 'w-3 h-3' : 'w-3.5 h-3.5'} text-amber-600`} />
          No Response
        </span>
      );
    case 'CALLING':
      return (
        <span className={`inline-flex items-center font-medium bg-sky-100 text-sky-800 rounded-full animate-pulse ${isSm ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'}`}>
          <PhoneCall className={`mr-1 ${isSm ? 'w-3 h-3' : 'w-3.5 h-3.5'} text-sky-600`} />
          Calling Now
        </span>
      );
    case 'SCHEDULED':
    default:
      return (
        <span className={`inline-flex items-center font-medium bg-slate-100 text-slate-700 rounded-full ${isSm ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'}`}>
          <Clock className={`mr-1 ${isSm ? 'w-3 h-3' : 'w-3.5 h-3.5'} text-slate-500`} />
          Scheduled
        </span>
      );
  }
};
