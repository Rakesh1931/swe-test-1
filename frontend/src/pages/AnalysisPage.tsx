import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { apiGet } from '../api/client';
import { EcoScoreGauge } from '../components/EcoScoreGauge';
import { IssueCard, StaticIssue } from '../components/IssueCard';
import { MetricsChart } from '../components/MetricsChart';
import { Download, AlertTriangle, Cpu, HardDrive, Database, Wifi, ShieldAlert, ArrowLeft } from 'lucide-react';

export const AnalysisPage: React.FC = () => {
  const { runId } = useParams<{ runId: string }>();

  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'all' | 'HIGH' | 'MEDIUM' | 'LOW'>('all');

  useEffect(() => {
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
    fetchResult();
  }, [runId]);

  if (loading) return <div className="text-center py-16 text-slate-400">Loading analysis results...</div>;
  if (!result) return <div className="text-center py-16 text-slate-400">Analysis result not found.</div>;

  const score = result.ecoScore || { totalScore: 75, cpuScore: 80, memoryScore: 70, databaseScore: 60, networkScore: 90, staticScore: 75 };
  const issues: StaticIssue[] = result.issues || [];
  const metrics = result.metrics || [];

  const filteredIssues = activeTab === 'all' ? issues : issues.filter((i) => i.severity === activeTab);

  const handleExport = () => {
    window.open(`http://localhost:8080/api/reports/${runId}?format=json`, '_blank');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <Link
          to={`/projects/${result.projectId}`}
          className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Project
        </Link>

        <button
          onClick={handleExport}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold border border-slate-700 transition-colors"
        >
          <Download className="w-4 h-4" /> Export Report (JSON)
        </button>
      </div>

      {result.status === 'FAILED' && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-3">
          <ShieldAlert className="w-6 h-6 text-rose-400 shrink-0" />
          <div>
            <h3 className="font-bold text-rose-200">Analysis Run Failed</h3>
            <p className="text-xs text-rose-300/80 mt-0.5">{result.error || 'The analysis could not complete. Verify your source path contains Java files.'}</p>
          </div>
        </div>
      )}

      {/* Top: EcoScore Gauge & Sub-Score Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="glass-card p-6 rounded-2xl border border-slate-800 flex flex-col items-center justify-center text-center">
          <EcoScoreGauge score={score.totalScore || 70} size={180} strokeWidth={14} />
          <p className="text-xs text-slate-400 mt-2">Aggregated Green-Software Performance Rating</p>
        </div>

        <div className="lg:col-span-2 glass-card p-6 rounded-2xl border border-slate-800 flex flex-col justify-between">
          <h2 className="text-base font-bold text-white mb-4">Sub-Score Breakdown</h2>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                <Cpu className="w-4 h-4 text-emerald-400" /> CPU Efficiency
              </div>
              <p className="text-2xl font-black text-white mt-1">{score.cpuScore ?? 80}</p>
            </div>

            <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                <HardDrive className="w-4 h-4 text-cyan-400" /> Memory Footprint
              </div>
              <p className="text-2xl font-black text-white mt-1">{score.memoryScore ?? 75}</p>
            </div>

            <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                <Database className="w-4 h-4 text-amber-400" /> DB Optimization
              </div>
              <p className="text-2xl font-black text-white mt-1">{score.databaseScore ?? 60}</p>
            </div>

            <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                <Wifi className="w-4 h-4 text-purple-400" /> Network Payload
              </div>
              <p className="text-2xl font-black text-white mt-1">{score.networkScore ?? 90}</p>
            </div>

            <div className="bg-slate-900/80 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                <ShieldAlert className="w-4 h-4 text-rose-400" /> Static AST Score
              </div>
              <p className="text-2xl font-black text-white mt-1">{score.staticScore ?? 70}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Middle: Runtime Metrics Chart */}
      {metrics.length > 0 && (
        <div className="glass-card p-6 rounded-2xl border border-slate-800">
          <h2 className="text-base font-bold text-white mb-4">Runtime Profile (Docker Sandbox)</h2>
          <MetricsChart metrics={metrics} />
        </div>
      )}

      {/* Bottom: Detected Static Issues (EC101 - EC108) */}
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

        {filteredIssues.length === 0 ? (
          <div className="glass-card p-8 text-center rounded-2xl border border-slate-800 text-slate-400 text-sm">
            No static issues detected matching filter '{activeTab}'.
          </div>
        ) : (
          <div className="space-y-3">
            {filteredIssues.map((issue, idx) => (
              <IssueCard key={idx} issue={issue} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
