import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Leaf, LayoutDashboard, GitCompare, LogOut, Code2 } from 'lucide-react';

export const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const token = localStorage.getItem('eco_token');

  const handleLogout = () => {
    localStorage.removeItem('eco_token');
    navigate('/login');
  };

  return (
    <nav className="glass-panel sticky top-0 z-50 border-b border-slate-800 bg-slate-950/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-3 group">
            <div className="p-2 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 text-white shadow-lg shadow-emerald-500/20 group-hover:scale-105 transition-transform">
              <Leaf className="w-5 h-5" />
            </div>
            <div>
              <span className="text-xl font-bold bg-gradient-to-r from-emerald-400 via-teal-200 to-white bg-clip-text text-transparent">
                EcoCode
              </span>
              <span className="ml-2 text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                v1.0
              </span>
            </div>
          </Link>

          <div className="flex items-center gap-6">
            <Link
              to="/dashboard"
              className="flex items-center gap-2 text-sm font-medium text-slate-300 hover:text-emerald-400 transition-colors"
            >
              <LayoutDashboard className="w-4 h-4" />
              Dashboard
            </Link>
            <Link
              to="/compare"
              className="flex items-center gap-2 text-sm font-medium text-slate-300 hover:text-emerald-400 transition-colors"
            >
              <GitCompare className="w-4 h-4" />
              Compare Runs
            </Link>

            {token ? (
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 text-sm font-medium text-slate-400 hover:text-rose-400 transition-colors px-3 py-1.5 rounded-lg border border-slate-800 hover:border-rose-500/30"
              >
                <LogOut className="w-4 h-4" />
                Logout
              </button>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-2 text-sm font-semibold px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 transition-all shadow-md shadow-emerald-500/20"
              >
                <Code2 className="w-4 h-4" />
                Sign In
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};
