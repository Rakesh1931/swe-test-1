import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { apiGet, BASE } from '../api/client';
import { EcoScoreGauge } from '../components/EcoScoreGauge';
import { IssueCard, StaticIssue } from '../components/IssueCard';
import { MetricsChart } from '../components/MetricsChart';
import { Download, AlertTriangle, Cpu, HardDrive, Database, Wifi, ShieldAlert, ArrowLeft, Loader2, CheckCircle2, Info } from 'lucide-react';

export const AnalysisPage: React.FC = () => {
  const { runId } = useParams<{ runId: string }>();

  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'all' | 'HIGH' | 'MEDIUM' | 'LOW'>('all');

  const fetchResult = async () => {
    try {
      const data = await apiGet<any>(`/analysis/${runId}/result`);
      setResult(data);
    } catch (err) {
      console.error('Failed to load analysis result:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResult();
  }, [runId]);

  // Auto-poll if analysis is still running
  useEffect(() => {
    if (result && result.status === 'RUNNING') {
      const timer = setInterval(() => {
        fetchResult();
      }, 3000);
      return () => clearInterval(timer);
    }
  }, [result?.status]);

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3 text-slate-400">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-400" />
        <p className="text-sm">Loading analysis results for Run #{runId}...</p>
      </div>
    );
  }

  if (!result) {
    return (
      <div className="max-w-4xl mx-auto py-16 text-center text-slate-400">
        <h2 className="text-lg font-bold text-white mb-2">Analysis Run Not Found</h2>
        <p className="text-sm mb-6">Could not retrieve details for Run #{runId}.</p>
        <Link
          to="/dashboard"
          className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs"
        >
          Back to Dashboard
        </Link>
      </div>
    );
  }

  const score = result.ecoScore; // Genuine EcoScore from DB, no fake dummy fallback!
  const issues: StaticIssue[] = result.issues || [];
  const metrics = result.metrics || [];
  const isFailed = result.status === 'FAILED';
  const isRunning = result.status === 'RUNNING';

  const filteredIssues = activeTab === 'all' ? issues : issues.filter((i) => i.severity === activeTab);

  const handleExport = () => {
    window.open(`${BASE}/reports/${runId}?format=json`, '_blank');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <Link
          to={result.projectId ? `/projects/${result.projectId}` : '/dashboard'}
          className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Project
        </Link>

        {result.status === 'COMPLETED' && (
          <button
            onClick={handleExport}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold border border-slate-700 transition-colors"
          >
            <Download className="w-4 h-4" /> Export Report (JSON)
          </button>
        )}
      </div>

      {/* Analysis Running Banner */}
      {isRunning && (
        <div className="p-6 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-200 flex items-center gap-4">
          <Loader2 className="w-7 h-7 animate-spin text-amber-400 shrink-0" />
          <div>
            <h3 className="font-bold text-base text-amber-200">Analysis In Progress</h3>
            <p className="text-xs text-amber-300/80 mt-1">
              EcoCode is parsing the Java AST, checking rules EC101-EC108, and profiling runtime footprint in the Docker sandbox. This page will update automatically.
            </p>
          </div>
        </div>
      )}

      {/* Analysis Failed Banner */}
      {isFailed && (
        <div className="p-6 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-300 space-y-2">
          <div className="flex items-center gap-3">
            <ShieldAlert className="w-6 h-6 text-rose-400 shrink-0" />
            <h3 className="font-bold text-base text-rose-200">Analysis Run #{runId} Failed</h3>
          </div>
          <p className="text-xs text-rose-300/90 pl-9 font-mono">
            {result.error || 'The analysis encountered an error. Verify the repository URL or directory path and try again.'}
          </p>
          <p className="text-xs text-slate-400 pl-9 pt-2">
            No EcoScore or static metrics were computed because the scan could not complete.
          </p>
        </div>
      )}

      {/* COMPLETED: EcoScore & Sub-Score Cards */}
      {result.status === 'COMPLETED' && score && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="glass-card p-6 rounded-2xl border border-slate-800 flex flex-col items-center justify-center text-center">
              <EcoScoreGauge score={score.totalScore} size={180} strokeWidth={14} />
              <p className="text-xs text-slate-400 mt-2 font-medium">Composite Green-Software Rating</p>
            </div>

            <div className="lg:col-span-2 glass-card p-6 rounded-2xl border border-slate-800 flex flex-col justify-between">
              <div>
                <h2 className="text-base font-bold text-white mb-1">Sub-Score Breakdown</h2>
                <p className="text-xs text-slate-400 mb-4">Individual efficiency metrics (scale 0 - 100)</p>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <Cpu className="w-4 h-4 text-emerald-400" /> CPU Efficiency
                  </div>
                  <p className="text-2xl font-black text-white mt-1">{score.cpuScore ?? '—'}</p>
                </div>

                <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <HardDrive className="w-4 h-4 text-cyan-400" /> Memory Footprint
                  </div>
                  <p className="text-2xl font-black text-white mt-1">{score.memoryScore ?? '—'}</p>
                </div>

                <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <Database className="w-4 h-4 text-amber-400" /> DB Optimization
                  </div>
                  <p className="text-2xl font-black text-white mt-1">{score.databaseScore ?? '—'}</p>
                </div>

                <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <Wifi className="w-4 h-4 text-purple-400" /> Network Payload
                  </div>
                  <p className="text-2xl font-black text-white mt-1">{score.networkScore ?? '—'}</p>
                </div>

                <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <ShieldAlert className="w-4 h-4 text-rose-400" /> Static AST Score
                  </div>
                  <p className="text-2xl font-black text-white mt-1">{score.staticScore ?? '—'}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Runtime Metrics Chart */}
          {metrics.length > 0 && (
            <div className="glass-card p-6 rounded-2xl border border-slate-800">
              <h2 className="text-base font-bold text-white mb-4">Runtime Profile (Docker Sandbox)</h2>
              <MetricsChart metrics={metrics} />
            </div>
          )}

          {/* Detected Static Issues */}
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <h2 className="text-xl font-extrabold text-white flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-amber-400" /> Static Analysis Inefficiencies ({issues.length})
              </h2>

              <div className="flex items-center gap-1 bg-slate-900/80 p-1 rounded-xl border border-slate-800 text-xs">
                {(['all', 'HIGH', 'MEDIUM', 'LOW'] as const).map((tab) => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab)}
                    className={`px-3 py-1 rounded-lg font-bold capitalize transition-all ${
                      activeTab === tab ? 'bg-emerald-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    {tab}
                  </button>
                ))}
              </div>
            </div>

            {issues.length === 0 ? (
              <div className="glass-card p-8 rounded-2xl border border-slate-800 text-center space-y-2">
                <div className="w-12 h-12 rounded-full bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mx-auto text-emerald-400">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="text-base font-bold text-slate-100">No Static Inefficiencies Detected</h3>
                <p className="text-xs text-slate-400 max-w-lg mx-auto">
                  Your code passed all deterministic rules (EC101-EC108). The AST has no string concatenations in loops, unbounded collections, unclosed resources, or repeated allocations. The Static AST score is 100/100!
                </p>
              </div>
            ) : filteredIssues.length === 0 ? (
              <div className="glass-card p-8 text-center rounded-2xl border border-slate-800 text-slate-400 text-sm">
                No issues found matching severity filter '{activeTab}'.
              </div>
            ) : (
              <div className="space-y-3">
                {filteredIssues.map((issue, idx) => (
                  <IssueCard key={idx} issue={issue} />
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};
