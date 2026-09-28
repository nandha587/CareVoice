import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types';
import { api } from '../api/client';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (fullName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('carevoice_token');
    const savedUser = localStorage.getItem('carevoice_user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.removeItem('carevoice_token');
        localStorage.removeItem('carevoice_user');
      }
    }
    setIsLoading(false);
  }, []);

  const handleAuthSuccess = (res: AuthResponse) => {
    const newUser: User = {
      id: res.userId,
      email: res.email,
      fullName: res.fullName,
      role: res.role,
    };
    setToken(res.token);
    setUser(newUser);
    localStorage.setItem('carevoice_token', res.token);
    localStorage.setItem('carevoice_user', JSON.stringify(newUser));
  };

  const login = async (email: string, password: string) => {
    const res = await api.login({ email, password });
    handleAuthSuccess(res);
  };

  const register = async (fullName: string, email: string, password: string) => {
    const res = await api.register({ fullName, email, password });
    handleAuthSuccess(res);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('carevoice_token');
    localStorage.removeItem('carevoice_user');
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
