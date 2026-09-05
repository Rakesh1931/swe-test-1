import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { apiGet } from '../api/client';
import { ComparisonTable, MetricDiff } from '../components/ComparisonTable';
import { IssueCard, StaticIssue } from '../components/IssueCard';
import { GitCompare, CheckCircle, AlertOctagon, MinusCircle, ArrowLeft } from 'lucide-react';

export const ComparisonPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const runA = searchParams.get('runA');
  const runB = searchParams.get('runB');

  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!runA || !runB) return;
    const fetchComparison = async () => {
      try {
        const res = await apiGet<any>(`/analysis/compare?runA=${runA}&runB=${runB}`);
        setData(res);
      } catch (err) {
        console.error('Failed to load comparison data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchComparison();
  }, [runA, runB]);

  if (!runA || !runB) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center text-slate-400">
        Please select two runs from a project page to compare versions.
      </div>
    );
  }

  if (loading) return <div className="text-center py-16 text-slate-400">Comparing run #{runA} vs run #{runB}...</div>;
  if (!data) return <div className="text-center py-16 text-slate-400">Could not load comparison data.</div>;

  const metrics: MetricDiff[] = data.metrics || [];
  const resolved: StaticIssue[] = data.resolvedIssues || [];
  const newIssues: StaticIssue[] = data.newIssues || [];
  const unchanged: StaticIssue[] = data.unchangedIssues || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="flex items-center justify-between">
        <Link to="/dashboard" className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white">
          <ArrowLeft className="w-4 h-4" /> Back to Dashboard
        </Link>
        <span className="text-xs font-mono text-slate-400">
          Comparing Run #{data.runAId} ({data.runABranch || 'main'}) ➔ Run #{data.runBId} ({data.runBBranch || 'main'})
        </span>
      </div>

      {/* Header Banner */}
      <div className="glass-card p-6 rounded-2xl border border-slate-800 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
            <GitCompare className="w-6 h-6 text-emerald-400" /> Version Comparison Report
          </h1>
          <p className="text-sm text-slate-400 mt-1">Demonstrates efficiency changes between software revisions</p>
        </div>

        {data.scoreA !== undefined && data.scoreB !== undefined && (
          <div className="flex items-center gap-4 text-right">
            <div>
              <p className="text-xs text-slate-400 uppercase">Before</p>
              <p className="text-xl font-bold text-slate-300">{data.scoreA} pts</p>
            </div>
            <span className="text-slate-600 text-lg">➔</span>
            <div>
              <p className="text-xs text-slate-400 uppercase">After</p>
              <p className="text-2xl font-black text-emerald-400">{data.scoreB} pts</p>
            </div>
          </div>
        )}
      </div>

      {/* Metrics Comparison Table */}
      <div className="glass-card p-6 rounded-2xl border border-slate-800">
        <h2 className="text-lg font-bold text-white mb-4">Runtime Metrics Comparison</h2>
        <ComparisonTable metrics={metrics} scoreA={data.scoreA} scoreB={data.scoreB} />
      </div>

      {/* Issue Diff Sections */}
      <div className="space-y-6">
        {/* Resolved Issues */}
        {resolved.length > 0 && (
          <div className="space-y-3">
            <h3 className="text-base font-bold text-emerald-400 flex items-center gap-2">
              <CheckCircle className="w-5 h-5" /> Resolved Issues ({resolved.length})
            </h3>
            <div className="space-y-3">
              {resolved.map((issue, idx) => (
                <IssueCard key={idx} issue={issue} />
              ))}
            </div>
          </div>
        )}

        {/* New Issues */}
        {newIssues.length > 0 && (
          <div className="space-y-3">
            <h3 className="text-base font-bold text-rose-400 flex items-center gap-2">
              <AlertOctagon className="w-5 h-5" /> New Bottlenecks Introduced ({newIssues.length})
            </h3>
            <div className="space-y-3">
              {newIssues.map((issue, idx) => (
                <IssueCard key={idx} issue={issue} />
              ))}
            </div>
          </div>
        )}

        {/* Unchanged Issues */}
        {unchanged.length > 0 && (
          <div className="space-y-3">
            <h3 className="text-base font-bold text-slate-400 flex items-center gap-2">
              <MinusCircle className="w-5 h-5" /> Unchanged Issues ({unchanged.length})
            </h3>
            <div className="space-y-3">
              {unchanged.map((issue, idx) => (
                <IssueCard key={idx} issue={issue} />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
