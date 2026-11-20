import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { apiGet, apiPost } from '../api/client';
import { Play, GitCompare, ExternalLink, Clock, ChevronRight, Folder, GitBranch, AlertCircle } from 'lucide-react';

export interface AnalysisRunItem {
  id: number;
  status: string;
  sourcePath?: string;
  branch?: string;
  createdAt: string;
  finishedAt?: string;
}

export const ProjectPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [project, setProject] = useState<any>(null);
  const [runs, setRuns] = useState<AnalysisRunItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [selectedRuns, setSelectedRuns] = useState<number[]>([]);
  const [starting, setStarting] = useState(false);
  const [sourcePath, setSourcePath] = useState('');
  const [repoUrl, setRepoUrl] = useState('');
  const [branch, setBranch] = useState('main');
  const [error, setError] = useState('');

  const fetchDetails = async () => {
    try {
      const data = await apiGet<any>(`/projects/${id}`);
      setProject(data.project);
      if (data.project?.repoUrl && !repoUrl) {
        setRepoUrl(data.project.repoUrl);
      }
      setRuns(data.runs || []);
    } catch (err: any) {
      console.error('Failed to load project details:', err);
      setError(err.message || 'Failed to load project details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  const handleStartAnalysis = async () => {
    setError('');
    const token = localStorage.getItem('eco_token');
    if (!token) {
      navigate('/login', {
        state: { notice: 'Please sign in or register to execute an analysis run.' },
      });
      return;
    }

    const cleanSource = sourcePath.trim();
    const cleanRepo = repoUrl.trim();

    if (!cleanSource && !cleanRepo) {
      setError('Please provide either a local source path or a repository URL.');
      return;
    }

    setStarting(true);
    try {
      const res = await apiPost<{ analysisRunId: number }>('/analysis/start', {
        projectId: Number(id),
        sourcePath: cleanSource || undefined,
        repoUrl: cleanRepo || undefined,
        branch: branch.trim() || 'main',
      });
      fetchDetails();
      if (res.analysisRunId) {
        navigate(`/analysis/${res.analysisRunId}`);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to start analysis.');
    } finally {
      setStarting(false);
    }
  };

  const toggleSelectRun = (runId: number) => {
    if (selectedRuns.includes(runId)) {
      setSelectedRuns(selectedRuns.filter((r) => r !== runId));
    } else {
      if (selectedRuns.length >= 2) {
        setSelectedRuns([selectedRuns[1], runId]);
      } else {
        setSelectedRuns([...selectedRuns, runId]);
      }
    }
  };

  const handleCompare = () => {
    if (selectedRuns.length === 2) {
      navigate(`/compare?runA=${selectedRuns[0]}&runB=${selectedRuns[1]}`);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center gap-3 text-slate-400">
        <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin" />
        <p className="text-sm">Loading project details...</p>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="max-w-xl mx-auto py-20 text-center space-y-4">
        <h2 className="text-xl font-bold text-white">Project Not Available</h2>
        <p className="text-xs text-slate-400">
          {error || 'The requested project could not be found, or you do not have permission to view it.'}
        </p>
        <Link
          to="/dashboard"
          className="inline-block px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold text-xs transition-colors"
        >
          Return to Dashboard
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="glass-card p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-extrabold text-white">{project.name}</h1>
            <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              {project.language}
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">{project.description || 'Java application registered for EcoCode audit.'}</p>
          {project.repoUrl && (
            <a
              href={project.repoUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 text-xs font-mono text-cyan-400 hover:underline mt-2"
            >
              {project.repoUrl} <ExternalLink className="w-3 h-3" />
            </a>
          )}
        </div>
      </div>

      {/* Start Analysis Configuration Box */}
      <div className="glass-card p-6 rounded-2xl border border-slate-800 space-y-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <Play className="w-5 h-5 text-emerald-400 fill-emerald-400/20" /> Run New Analysis
        </h2>
        <p className="text-xs text-slate-400">
          Enter either a local directory path to scan, or a remote Git repository URL.
        </p>

        {error && (
          <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
          {/* Option A: Local Source Path */}
          <div className="space-y-3 bg-slate-900/60 p-4 rounded-xl border border-slate-800">
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-2">
              <Folder className="w-4 h-4 text-emerald-400" /> Local Directory Path
            </label>
            <input
              type="text"
              placeholder="e.g. C:/projects/my-java-app or /home/user/project"
              value={sourcePath}
              onChange={(e) => setSourcePath(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-xs font-mono text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500"
            />
            <p className="text-[11px] text-slate-500">
              Provide an absolute or relative directory path containing Java source files.
            </p>
          </div>

          {/* Option B: Git Repository URL */}
          <div className="space-y-3 bg-slate-900/60 p-4 rounded-xl border border-slate-800">
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-2">
              <GitBranch className="w-4 h-4 text-cyan-400" /> Git Repository & Branch
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="https://github.com/org/repo.git"
                value={repoUrl}
                onChange={(e) => setRepoUrl(e.target.value)}
                className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-xs font-mono text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500"
              />
              <input
                type="text"
                placeholder="main"
                value={branch}
                onChange={(e) => setBranch(e.target.value)}
                className="w-24 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs font-mono text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500"
              />
            </div>
            <p className="text-[11px] text-slate-500">EcoCode will clone the target branch via JGit for isolated scanning.</p>
          </div>
        </div>

        <div className="pt-2 flex justify-end">
          <button
            onClick={handleStartAnalysis}
            disabled={starting}
            className="px-6 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold text-sm shadow-lg shadow-emerald-500/20 transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <Play className="w-4 h-4 fill-slate-950" />
            {starting ? 'Scanning Project...' : 'Start Analysis'}
          </button>
        </div>
      </div>

      {/* Compare Banner */}
      {selectedRuns.length > 0 && (
        <div className="glass-panel p-4 rounded-xl border border-cyan-500/30 flex items-center justify-between bg-cyan-950/20">
          <p className="text-xs text-cyan-200">
            Selected {selectedRuns.length} run(s) for version comparison.{' '}
            {selectedRuns.length === 1 && 'Select 1 more run to enable compare.'}
          </p>

          <button
            onClick={handleCompare}
            disabled={selectedRuns.length !== 2}
            className="px-4 py-1.5 rounded-lg bg-cyan-500 text-slate-950 font-bold text-xs hover:bg-cyan-400 disabled:opacity-50 flex items-center gap-1.5"
          >
            <GitCompare className="w-4 h-4" /> Compare Runs
          </button>
        </div>
      )}

      {/* Analysis History */}
      <div className="glass-card rounded-2xl border border-slate-800 overflow-hidden">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between">
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Clock className="w-5 h-5 text-emerald-400" /> Analysis History
          </h2>
          <span className="text-xs text-slate-400">{runs.length} runs executed</span>
        </div>

        {runs.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-sm">
            No analysis runs executed yet. Enter a source path or repo URL above and click "Start Analysis".
          </div>
        ) : (
          <div className="divide-y divide-slate-800/80">
            {runs.map((r) => {
              const isSelected = selectedRuns.includes(r.id);
              return (
                <div
                  key={r.id}
                  className={`p-4 flex items-center justify-between hover:bg-slate-900/40 transition-colors ${
                    isSelected ? 'bg-cyan-950/20 border-l-4 border-cyan-400' : ''
                  }`}
                >
                  <div className="flex items-center gap-4">
                    <input
                      type="checkbox"
                      checked={isSelected}
                      onChange={() => toggleSelectRun(r.id)}
                      className="rounded border-slate-700 bg-slate-900 text-emerald-500 focus:ring-0"
                    />

                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-sm font-bold text-slate-200">Run #{r.id}</span>
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                            r.status === 'COMPLETED'
                              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                              : r.status === 'RUNNING'
                              ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                              : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                          }`}
                        >
                          {r.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-1">
                        {new Date(r.createdAt).toLocaleString()} • Target: {r.sourcePath || 'Git Repo'}
                      </p>
                    </div>
                  </div>

                  <Link
                    to={`/analysis/${r.id}`}
                    className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 hover:underline bg-emerald-500/10 px-3 py-1.5 rounded-lg border border-emerald-500/20"
                  >
                    View Report <ChevronRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
