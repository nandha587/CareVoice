import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import { NotificationAlert } from '../types';
import { PhoneCall, HeartPulse, Users, Pill, BarChart3, Bell, LogOut, PhoneForwarded } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [alerts, setAlerts] = useState<NotificationAlert[]>([]);
  const [showAlertMenu, setShowAlertMenu] = useState(false);

  useEffect(() => {
    if (user) {
      api.getNotifications()
        .then(data => setAlerts(data.slice(0, 5)))
        .catch(console.error);
    }
  }, [user, location.pathname]);

  const navLinks = [
    { name: 'Dashboard', path: '/', icon: HeartPulse },
    { name: 'Elderly Patients', path: '/patients', icon: Users },
    { name: 'Medications', path: '/medications', icon: Pill },
    { name: 'Live Call Simulator', path: '/simulator', icon: PhoneForwarded },
    { name: 'Weekly Report', path: '/reports', icon: BarChart3 },
  ];

  return (
    <nav className="bg-white border-b border-slate-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center space-x-8">
            <Link to="/" className="flex items-center space-x-2 text-green-700 hover:text-green-800 transition">
              <div className="w-10 h-10 rounded-xl bg-green-100 flex items-center justify-center text-green-600 shadow-sm">
                <PhoneCall className="w-5 h-5 stroke-[2.5]" />
              </div>
              <div>
                <span className="text-xl font-bold tracking-tight text-slate-900">Care<span className="text-green-600">Voice</span></span>
                <span className="block text-[10px] uppercase font-semibold text-slate-400 tracking-wider">Voice-First Adherence</span>
              </div>
            </Link>

            <div className="hidden md:flex space-x-1">
              {navLinks.map((link) => {
                const Icon = link.icon;
                const isActive = location.pathname === link.path;
                return (
                  <Link
                    key={link.path}
                    to={link.path}
                    className={`inline-flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                      isActive
                        ? 'bg-green-50 text-green-700 font-semibold'
                        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                    }`}
                  >
                    <Icon className="w-4 h-4 mr-2" />
                    {link.name}
                  </Link>
                );
              })}
            </div>
          </div>

          <div className="flex items-center space-x-4">
            {/* Alerts Dropdown */}
            <div className="relative">
              <button
                onClick={() => setShowAlertMenu(!showAlertMenu)}
                className="relative p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-full transition"
                title="Notifications"
              >
                <Bell className="w-5 h-5" />
                {alerts.length > 0 && (
                  <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-red-500 rounded-full ring-2 ring-white"></span>
                )}
              </button>

              {showAlertMenu && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-slate-200 py-2 z-50 animate-in fade-in slide-in-from-top-2">
                  <div className="px-4 py-2 border-b border-slate-100 font-semibold text-xs text-slate-500 uppercase tracking-wider">
                    Recent Caregiver Alerts
                  </div>
                  {alerts.length === 0 ? (
                    <div className="px-4 py-6 text-center text-sm text-slate-400">
                      No active alerts. All reminders on track!
                    </div>
                  ) : (
                    <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
                      {alerts.map((alert) => (
                        <div key={alert.id} className="p-3 hover:bg-slate-50 text-xs">
                          <div className="font-semibold text-slate-800 flex items-center justify-between">
                            <span className={alert.type.includes('MISSED') || alert.type.includes('NO_RESPONSE') ? 'text-red-600' : 'text-amber-600'}>
                              {alert.type.replace(/_/g, ' ')}
                            </span>
                            <span className="text-[10px] text-slate-400">
                              {new Date(alert.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <p className="text-slate-600 mt-1">{alert.message}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Caregiver Profile */}
            <div className="flex items-center pl-3 border-l border-slate-200 space-x-3">
              <div className="text-right hidden sm:block">
                <div className="text-sm font-semibold text-slate-800">{user?.fullName || 'Caregiver'}</div>
                <div className="text-xs text-slate-400">{user?.email}</div>
              </div>
              <button
                onClick={logout}
                className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                title="Log out"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};
