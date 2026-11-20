import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiGet, apiPost } from '../api/client';
import { Plus, FolderGit2, AlertTriangle, ShieldCheck, Activity, ChevronRight, X, Lock, LogIn } from 'lucide-react';
import { EcoScoreGauge } from '../components/EcoScoreGauge';

export interface ProjectItem {
  id: number;
  name: string;
  description: string;
  repoUrl?: string;
  language: string;
  createdAt: string;
  lastEcoScore?: number;
  highIssues: number;
  mediumIssues: number;
  lowIssues: number;
  totalRuns: number;
}

export const DashboardPage: React.FC = () => {
  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [repoUrl, setRepoUrl] = useState('');
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  const navigate = useNavigate();
  const token = localStorage.getItem('eco_token');

  const fetchProjects = async () => {
    if (!token) {
      setProjects([]);
      setLoading(false);
      return;
    }

    try {
      const data = await apiGet<ProjectItem[]>('/projects');
      setProjects(data || []);
    } catch (err) {
      console.error('Failed to fetch projects:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, [token]);

  const handleOpenNewProject = () => {
    if (!token) {
      navigate('/login', {
        state: { notice: 'Please sign in or register to create and manage projects.' },
      });
      return;
    }
    setCreateError('');
    setModalOpen(true);
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');

    if (!token) {
      navigate('/login', {
        state: { notice: 'Please sign in or register to create a project.' },
      });
      return;
    }

    setCreating(true);
    try {
      await apiPost('/projects', { name, description, repoUrl });
      setName('');
      setDescription('');
      setRepoUrl('');
      setModalOpen(false);
      fetchProjects();
    } catch (err: any) {
      setCreateError(err.message || 'Failed to create project.');
    } finally {
      setCreating(false);
    }
  };

  // Honest score calculation: only average projects that actually have completed scores
  const analyzedProjects = projects.filter((p) => p.lastEcoScore !== undefined && p.lastEcoScore !== null);
  const avgEcoScore = analyzedProjects.length > 0
    ? Math.round(analyzedProjects.reduce((acc, p) => acc + (p.lastEcoScore || 0), 0) / analyzedProjects.length)
    : null;

  const totalHighIssues = projects.reduce((acc, p) => acc + (p.highIssues || 0), 0);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Guest Notice Banner if Not Signed In */}
      {!token && (
        <div className="mb-8 p-4 rounded-2xl bg-gradient-to-r from-emerald-950/60 via-slate-900/60 to-slate-900/60 border border-emerald-500/30 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Sign In to Manage Your Projects</h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Each developer has their own private workspace. Sign in or register to register and analyze your Java repositories.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Link
              to="/login"
              className="px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold text-xs shadow-md shadow-emerald-500/20 flex items-center gap-1.5"
            >
              <LogIn className="w-3.5 h-3.5" /> Sign In
            </Link>
            <Link
              to="/register"
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs border border-slate-700"
            >
              Register
            </Link>
          </div>
        </div>
      )}

      {/* Quick Stats Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Project Dashboard</h1>
          <p className="text-sm text-slate-400 mt-1">Overview of registered Java codebases and EcoScores</p>
        </div>

        <button
          onClick={handleOpenNewProject}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold text-sm shadow-lg shadow-emerald-500/20 transition-all"
        >
          <Plus className="w-4 h-4" />
          New Project
        </button>
      </div>

      {/* Stats Bar */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="glass-card p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 rounded-xl bg-blue-500/10 text-blue-400 border border-blue-500/20">
            <FolderGit2 className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">My Projects</p>
            <p className="text-2xl font-extrabold text-white mt-0.5">{projects.length}</p>
          </div>
        </div>

        <div className="glass-card p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Average EcoScore</p>
            <p className="text-2xl font-extrabold text-emerald-400 mt-0.5">
              {avgEcoScore !== null ? `${avgEcoScore} / 100` : '—'}
            </p>
          </div>
        </div>

        <div className="glass-card p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400 border border-rose-500/20">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">High Severity Bottlenecks</p>
            <p className="text-2xl font-extrabold text-rose-400 mt-0.5">{totalHighIssues}</p>
          </div>
        </div>
      </div>

      {/* Projects Grid */}
      {loading ? (
        <div className="text-center py-16 text-slate-400 text-sm">Loading projects...</div>
      ) : projects.length === 0 ? (
        <div className="glass-card p-12 text-center rounded-2xl border border-slate-800">
          <FolderGit2 className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-200">
            {!token ? 'Sign In to View Your Projects' : 'No Projects Found'}
          </h3>
          <p className="text-sm text-slate-400 mt-1 mb-6">
            {!token
              ? 'Each user has a private repository workspace. Sign in or register to get started.'
              : 'Create a project to start analyzing Java AST rules and runtime metrics.'}
          </p>
          <button
            onClick={handleOpenNewProject}
            className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-sm hover:bg-emerald-600 transition-all shadow-md shadow-emerald-500/20"
          >
            {!token ? 'Sign In / Register' : 'Create Project'}
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((p) => (
            <Link
              key={p.id}
              to={`/projects/${p.id}`}
              className="glass-card p-6 rounded-2xl border border-slate-800 hover:border-emerald-500/40 transition-all flex flex-col justify-between group relative overflow-hidden"
            >
              <div>
                <div className="flex items-start justify-between gap-4 mb-3">
                  <div>
                    <h3 className="text-lg font-bold text-slate-100 group-hover:text-emerald-400 transition-colors">
                      {p.name}
                    </h3>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      {p.language}
                    </span>
                  </div>

                  {p.lastEcoScore !== undefined && p.lastEcoScore !== null ? (
                    <EcoScoreGauge score={p.lastEcoScore} size={64} strokeWidth={6} showLabel={false} />
                  ) : (
                    <span className="text-[10px] font-semibold px-2 py-1 rounded-lg bg-slate-900 border border-slate-800 text-slate-400">
                      Not Analyzed
                    </span>
                  )}
                </div>

                <p className="text-xs text-slate-400 line-clamp-2 mb-4 leading-relaxed">
                  {p.description || 'No description provided.'}
                </p>

                {p.repoUrl && (
                  <p className="text-[11px] font-mono text-slate-500 truncate mb-4">
                    {p.repoUrl}
                  </p>
                )}
              </div>

              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
                <div className="flex items-center gap-2">
                  <span className="text-rose-400 font-bold">{p.highIssues || 0} High</span>
                  <span>•</span>
                  <span className="text-amber-400 font-bold">{p.mediumIssues || 0} Med</span>
                </div>

                <span className="flex items-center gap-1 text-emerald-400 font-medium group-hover:translate-x-1 transition-transform">
                  View Runs ({p.totalRuns}) <ChevronRight className="w-3.5 h-3.5" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* New Project Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="glass-card w-full max-w-lg p-6 rounded-2xl border border-slate-800 relative">
            <button
              onClick={() => setModalOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-xl font-bold text-white mb-4">Register New Java Project</h2>

            {createError && (
              <div className="mb-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium">
                {createError}
              </div>
            )}

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1">
                  Project Name
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Student Microservice"
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1">
                  Description
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Brief description of application domain"
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-slate-100 focus:outline-none focus:border-emerald-500 h-20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1">
                  Repository URL (Optional)
                </label>
                <input
                  type="text"
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  placeholder="https://github.com/BrownBoy90/UCS503P-202627-EcoCode"
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-slate-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-slate-400 hover:text-white text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-sm hover:bg-emerald-600 disabled:opacity-50"
                >
                  {creating ? 'Creating...' : 'Create Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
